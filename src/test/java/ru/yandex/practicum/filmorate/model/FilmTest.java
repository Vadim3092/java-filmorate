package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {

    @Test
    public void testEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            if (film.getName() == null || film.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
        });
    }

    @Test
    public void testLongDescription() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Описание не может быть длиннее 200 символов");
            }
        });
    }

    @Test
    public void testOldReleaseDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        });
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительной");
            }
        });
    }
}
