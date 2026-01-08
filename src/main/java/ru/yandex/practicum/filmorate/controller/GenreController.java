package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenreController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/genres")
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT id AS id, name AS name FROM genre");
    }

    @GetMapping("/genres/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        return jdbcTemplate.queryForMap("SELECT id AS id, name AS name FROM genre WHERE id = ?", id);
    }
}
