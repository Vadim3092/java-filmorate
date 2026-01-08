package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT id, name FROM genre ORDER BY id");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT id, name FROM genre WHERE id = ?", id);
        if (list.isEmpty()) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр не найден");
        }
        return list.get(0);
    }
}
