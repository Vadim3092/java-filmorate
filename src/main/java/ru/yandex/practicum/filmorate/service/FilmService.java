package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        validateFilm(film);
        validateMpaExists(film.getMpaId());
        if (film.getGenreIds() != null) {
            for (Integer genreId : film.getGenreIds()) {
                validateGenreExists(genreId);
            }
        }
        return filmStorage.save(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        validateMpaExists(film.getMpaId());
        if (film.getGenreIds() != null) {
            for (Integer genreId : film.getGenreIds()) {
                validateGenreExists(genreId);
            }
        }
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId); // проверяем что пользователь существует
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId); // проверяем что пользователь существует
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<GenreDto> getAllGenres() {
        String sql = "SELECT id, name FROM genre ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new GenreDto(rs.getInt("id"), rs.getString("name"))
        );
    }

    public GenreDto getGenreById(int id) {
        String sql = "SELECT id, name FROM genre WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new GenreDto(rs.getInt("id"), rs.getString("name")), id);
        } catch (Exception e) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр с id=" + id + " не найден");
        }
    }

    public List<MpaDto> getAllMpa() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaDto(rs.getInt("id"), rs.getString("name"))
        );
    }

    public MpaDto getMpaById(int id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new MpaDto(rs.getInt("id"), rs.getString("name")), id);
        } catch (Exception e) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("MPA с id=" + id + " не найден");
        }
    }

    public FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikes(film.getLikes());

        if (film.getMpaId() != null) {
            dto.setMpa(getMpaById(film.getMpaId()));
        }

        Set<GenreDto> genres = new HashSet<>();
        if (film.getGenreIds() != null) {
            for (Integer genreId : film.getGenreIds()) {
                genres.add(getGenreById(genreId));
            }
        }
        dto.setGenres(genres);

        return dto;
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

    private void validateMpaExists(Integer mpaId) {
        if (mpaId == null) return;
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa WHERE id = ?", Integer.class, mpaId);
        if (count == 0) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("MPA с id=" + mpaId + " не найден");
        }
    }

    private void validateGenreExists(Integer genreId) {
        if (genreId == null) return;
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genre WHERE id = ?", Integer.class, genreId);
        if (count == 0) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр с id=" + genreId + " не найден");
        }
    }
}