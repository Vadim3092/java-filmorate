package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT id, name FROM mpa ORDER BY id");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT id, name FROM mpa WHERE id = ?", id);
        if (list.isEmpty()) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("MPA не найден");
        }
        return list.get(0);
    }
}
