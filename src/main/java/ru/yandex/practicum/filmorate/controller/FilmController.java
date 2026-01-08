package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll().stream()
                .map(filmService::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Long id) {
        return filmService.toDto(filmService.findById(id));
    }

    @PostMapping
    public FilmDto create(@RequestBody Film film) {
        return filmService.toDto(filmService.create(film));
    }

    @PutMapping
    public FilmDto update(@RequestBody Film film) {
        return filmService.toDto(filmService.update(film));
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
        return filmService.getPopular(count).stream()
                .map(filmService::toDto)
                .collect(Collectors.toList());
    }
}
