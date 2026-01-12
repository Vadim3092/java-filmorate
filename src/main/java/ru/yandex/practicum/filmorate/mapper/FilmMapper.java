package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

public class FilmMapper {

    public static Film toFilm(FilmCreateDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpa() != null) {
            film.setMpaId(dto.getMpa().getId());
        }

        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Integer> genreIds = new HashSet<>();
            for (var genre : dto.getGenres()) {
                genreIds.add(genre.getId());
            }
            film.setGenreIds(genreIds);
        }

        return film;
    }

    public static FilmDto toDto(Film film, Map<Integer, MpaDto> mpaMap, Map<Integer, GenreDto> genreMap) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikes(film.getLikes());

        if (film.getMpaId() != null) {
            dto.setMpa(mpaMap.get(film.getMpaId()));
        }

        Set<GenreDto> genres = new LinkedHashSet<>();
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            List<Integer> sortedGenreIds = film.getGenreIds().stream()
                    .sorted()
                    .collect(Collectors.toList());
            for (Integer id : sortedGenreIds) {
                genres.add(genreMap.get(id));
            }
        }
        dto.setGenres(genres);

        return dto;
    }
}
