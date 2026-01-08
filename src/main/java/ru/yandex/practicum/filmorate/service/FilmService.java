package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        return filmStorage.save(film);
    }

    public Film update(Film film) {
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
}
