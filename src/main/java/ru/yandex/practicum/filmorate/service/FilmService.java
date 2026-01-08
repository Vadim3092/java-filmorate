package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
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
        validateMpaAndGenres(film);
        return filmStorage.save(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId);
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
            throw new NotFoundException("Жанр с id=" + id + " не найден");
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
            throw new NotFoundException("MPA с id=" + id + " не найден");
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
        
        Set<GenreDto> genres = new LinkedHashSet<>();
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            List<Integer> sortedGenreIds = film.getGenreIds().stream()
                    .sorted()
                    .collect(Collectors.toList());
            for (Integer genreId : sortedGenreIds) {
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

    private void validateMpaAndGenres(Film film) {
        if (film.getMpaId() != null) {
            try {
                getMpaById(film.getMpaId());
            } catch (NotFoundException e) {
                throw new NotFoundException("MPA с id=" + film.getMpaId() + " не найден");
            }
        }

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer genreId : film.getGenreIds()) {
                try {
                    getGenreById(genreId);
                } catch (NotFoundException e) {
                    throw new NotFoundException("Жанр с id=" + genreId + " не найден");
                }
            }
        }
    }
}