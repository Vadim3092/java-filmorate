package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testEmailWithoutAt() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
        });
    }

    @Test
    public void testEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
        });
    }

    @Test
    public void testEmptyLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
        });
    }

    @Test
    public void testLoginWithSpace() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("log in");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
        });
    }

    @Test
    public void testFutureBirthday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        });
    }
}