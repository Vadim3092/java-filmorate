package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query("SELECT * FROM films", this::mapRow);
        for (Film film : films) {
            loadLikes(film);
            loadGenres(film);
        }
        return films;
    }

    @Override
    public Film findById(Long id) {
        List<Film> films = jdbcTemplate.query("SELECT * FROM films WHERE id = ?", this::mapRow, id);
        if (films.isEmpty()) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Фильм не найден");
        }
        Film film = films.get(0);
        loadLikes(film);
        loadGenres(film);
        return film;
    }

    @Override
    public Film save(Film film) {
        validateFilm(film);
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId()
        );
        Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Long.class);
        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        validateFilm(film);
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId(),
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private void saveGenres(Film film) {
        if (film.getGenreIds() != null) {
            for (Integer genreId : film.getGenreIds()) {
                jdbcTemplate.update(
                        "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genreId
                );
            }
        }
    }

    private void loadGenres(Film film) {
        List<Integer> genres = jdbcTemplate.queryForList(
                "SELECT genre_id FROM film_genres WHERE film_id = ?", Integer.class, film.getId()
        );
        film.setGenreIds(new HashSet<>(genres));
    }

    private void loadLikes(Film film) {
        List<Long> likes = jdbcTemplate.queryForList(
                "SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId()
        );
        film.setLikes(new HashSet<>(likes));
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ru.yandex.practicum.filmorate.exception.ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getInt("mpa_id"));
        film.setLikes(new HashSet<>());
        film.setGenreIds(new HashSet<>());
        return film;
    }
}
