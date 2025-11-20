package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private long nextId = 1;

    @GetMapping
    public List<Film> findAll() {
        return films;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.add(film);
        log.info("Добавлен фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validateFilm(film);
        int index = -1;
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId().equals(film.getId())) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
        films.set(index, film);
        log.info("Обновлён фильм: {}", film.getName());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
