package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
/* Логику с "@Qualifier" понял,
но решил сделать проще, просто удалил "@Сomponent"
 */
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private long nextId = 1;

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    @Override
    public Film save(Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Сохранён фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        validateFilm(film);
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film.getName());
        return film;
    }

    @Override
    public void deleteById(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        films.remove(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        film.getLikes().remove(userId);
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
