package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MpaController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/mpa")
    public List<Map<String, Object>> getAll() {
        return jdbcTemplate.queryForList("SELECT id AS id, name AS name FROM mpa");
    }

    @GetMapping("/mpa/{id}")
    public Map<String, Object> getById(@PathVariable int id) {
        try {
            return jdbcTemplate.queryForMap("SELECT id AS id, name AS name FROM mpa WHERE id = ?", id);
        } catch (EmptyResultDataAccessException e) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("MPA не найден");
        }
    }
}
