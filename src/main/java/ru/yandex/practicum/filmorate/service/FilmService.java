package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    private FilmDto toDto(Film film, Map<Integer, MpaDto> mpaMap, Map<Integer, GenreDto> genreMap) {
        return FilmMapper.toDto(film, mpaMap, genreMap);
    }

    public List<FilmDto> findAll() {
        List<Film> films = filmStorage.findAll();
        return convertFilmsToDtos(films);
    }

    public FilmDto findById(Long id) {
        Film film = filmStorage.findById(id);
        return convertFilmToDto(film);
    }

    public FilmDto create(FilmCreateDto filmCreateDto) {
        Film film = FilmMapper.toFilm(filmCreateDto);
        validateFilm(film);
        validateMpaAndGenres(film);
        Film savedFilm = filmStorage.save(film);
        return convertFilmToDto(savedFilm);
    }

    public FilmDto update(FilmCreateDto filmCreateDto) {
        Film film = FilmMapper.toFilm(filmCreateDto);
        validateFilm(film);
        validateMpaAndGenres(film);
        Film updatedFilm = filmStorage.update(film);
        return convertFilmToDto(updatedFilm);
    }

    public List<FilmDto> getPopular(int count) {
        List<Film> films = filmStorage.getPopular(count);
        return convertFilmsToDtos(films);
    }

    private List<FilmDto> convertFilmsToDtos(List<Film> films) {
        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, MpaDto> mpaMap = mpaStorage.getAllMpa().stream()
                .collect(Collectors.toMap(MpaDto::getId, m -> m));
        Map<Integer, GenreDto> genreMap = genreStorage.getAllGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, g -> g));

        return films.stream()
                .map(film -> toDto(film, mpaMap, genreMap))
                .collect(Collectors.toList());
    }

    private FilmDto convertFilmToDto(Film film) {
        List<FilmDto> result = convertFilmsToDtos(List.of(film));
        return result.get(0);
    }

    public Film findFilmModelById(Long id) {
        return filmStorage.findById(id);
    }

    public Film createFilmModel(Film film) {
        validateFilm(film);
        validateMpaAndGenres(film);
        return filmStorage.save(film);
    }

    public Film updateFilmModel(Film film) {
        validateFilm(film);
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        filmStorage.removeLike(filmId, userId);
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
                    throw new NotFoundException("Жанр с id=" + id + " не существует");
                }
            }
        }
    }
}