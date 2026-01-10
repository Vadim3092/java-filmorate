package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

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
        findById(filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        findById(filmId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    public List<GenreDto> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public GenreDto getGenreById(int id) {
        return genreStorage.getGenreById(id);
    }

    public List<MpaDto> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public MpaDto getMpaById(int id) {
        return mpaStorage.getMpaById(id);
    }

    public FilmDto toDto(Film film) {
        Map<Integer, MpaDto> mpaMap = getAllMpa().stream()
                .collect(Collectors.toMap(MpaDto::getId, m -> m));
        Map<Integer, GenreDto> genreMap = getAllGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, g -> g));

        return FilmMapper.toDto(film, mpaMap, genreMap);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpaId() != null) {
            mpaStorage.getMpaById(film.getMpaId());
        }

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            Set<Integer> validIds = genreStorage.getAllGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toSet());

            for (Integer id : film.getGenreIds()) {
                if (!validIds.contains(id)) {
                    throw new ValidationException("Жанр с id=" + id + " не существует");
                }
            }
        }
    }
}