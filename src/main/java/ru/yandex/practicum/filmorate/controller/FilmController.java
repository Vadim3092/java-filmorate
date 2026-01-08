package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> findAll() {
        return filmService.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Long id) {
        return toMap(filmService.findById(id));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Film film) {
        return toMap(filmService.create(film));
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody Film film) {
        return toMap(filmService.update(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Map<String, Object>> getPopular(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть больше 0");
        }
        return filmService.getPopular(count).stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(Film film) {
        Map<String, Object> filmMap = new HashMap<>();
        filmMap.put("id", film.getId());
        filmMap.put("name", film.getName());
        filmMap.put("description", film.getDescription());
        filmMap.put("releaseDate", film.getReleaseDate());
        filmMap.put("duration", film.getDuration());
        filmMap.put("likes", film.getLikes());

        // mpa
        Map<String, Object> mpa = jdbcTemplate.queryForMap(
                "SELECT id AS id, name AS name FROM mpa WHERE id = ?",
                film.getMpaId()
        );
        filmMap.put("mpa", mpa);

        // genres
        List<Map<String, Object>> genres = new ArrayList<>();
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            String ids = film.getGenreIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            genres = jdbcTemplate.queryForList(
                    "SELECT id AS id, name AS name FROM genre WHERE id IN (" + ids + ")"
            );
        }
        filmMap.put("genres", genres);

        return filmMap;
    }
}
