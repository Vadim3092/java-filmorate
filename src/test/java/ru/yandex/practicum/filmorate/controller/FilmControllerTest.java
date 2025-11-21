package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void emptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void goodFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Фильм о виртуальной реальности");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        Film result = controller.create(film);

        assertEquals("Матрица", result.getName());
        assertEquals(136, result.getDuration());
    }
}