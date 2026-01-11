package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.util.List;

public interface MpaStorage {

    List<MpaDto> getAllMpa();

    MpaDto getMpaById(int id);
}
