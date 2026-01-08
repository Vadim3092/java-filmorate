package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;

    public Film toFilm() {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);

        if (mpa != null) {
            film.setMpaId(mpa.getId());
        }

        if (genres != null) {
            Set<Integer> genreIds = new HashSet<>();
            for (GenreDto genre : genres) {
                genreIds.add(genre.getId());
            }
            film.setGenreIds(genreIds);
        }

        return film;
    }
}
