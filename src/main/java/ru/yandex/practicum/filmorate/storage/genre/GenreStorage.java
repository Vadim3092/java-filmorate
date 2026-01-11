package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.dto.GenreDto;

import java.util.List;

public interface GenreStorage {

    List<GenreDto> getAllGenres();

    GenreDto getGenreById(int id);
}