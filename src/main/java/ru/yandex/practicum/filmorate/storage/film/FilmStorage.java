package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film findById(Long id);

    Film save(Film film);

    Film update(Film film);

    void deleteById(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
