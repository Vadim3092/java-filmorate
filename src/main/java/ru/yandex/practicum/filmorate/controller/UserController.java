package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    @GetMapping
    public List<User> findAll() {
        return users;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        user.setId(nextId++);
        users.add(user);
        log.info("Добавлен пользователь: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        validateUser(user);
        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
        users.set(index, user);
        log.info("Обновлён пользователь: {}", user.getLogin());
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
