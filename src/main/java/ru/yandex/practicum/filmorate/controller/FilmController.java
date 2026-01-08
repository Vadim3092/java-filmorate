package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Long id) {
        return toDto(filmService.findById(id));
    }

    @PostMapping
    public FilmDto create(@RequestBody Film film) {
        return toDto(filmService.create(film));
    }

    @PutMapping
    public FilmDto update(@RequestBody Film film) {
        return toDto(filmService.update(film));
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
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count должен быть больше 0");
        }
        return filmService.getPopular(count).stream().map(this::toDto).collect(Collectors.toList());
    }

    private FilmDto toDto(Film film) {
        MpaDto mpa = null;
        if (film.getMpaId() != null) {
            try {
                mpa = jdbcTemplate.queryForObject(
                        "SELECT id AS id, name AS name FROM mpa WHERE id = ?",
                        (rs, rowNum) -> new MpaDto(rs.getInt("id"), rs.getString("name")),
                        film.getMpaId()
                );
            } catch (EmptyResultDataAccessException e) {

            }
        }

        Set<GenreDto> genres = new HashSet<>();
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer genreId : film.getGenreIds()) {
                try {
                    GenreDto g = jdbcTemplate.queryForObject(
                            "SELECT id AS id, name AS name FROM genre WHERE id = ?",
                            (rs, rowNum) -> new GenreDto(rs.getInt("id"), rs.getString("name")),
                            genreId
                    );
                    genres.add(g);
                } catch (EmptyResultDataAccessException e) {

                }
            }
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikes(film.getLikes());
        dto.setMpa(mpa);
        dto.setGenres(genres);
        return dto;
    }
}
