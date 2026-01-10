package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String FILM_SELECT_SQL =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "g.genre_id " +
                    "FROM films f " +
                    "LEFT JOIN film_genres g ON f.id = g.film_id ";

    @Override
    public List<Film> findAll() {
        Map<Long, Film> filmMap = jdbcTemplate.query(FILM_SELECT_SQL, new FilmResultSetExtractor());
        loadAllLikes(new ArrayList<>(filmMap.values()));
        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film findById(Long id) {
        String sql = FILM_SELECT_SQL + "WHERE f.id = ?";
        Map<Long, Film> filmMap = jdbcTemplate.query(sql, ps -> ps.setLong(1, id), new FilmResultSetExtractor());

        if (filmMap.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        Film film = filmMap.values().iterator().next();
        loadLikes(film);
        return film;
    }

    private static class FilmResultSetExtractor implements ResultSetExtractor<Map<Long, Film>> {
        @Override
        public Map<Long, Film> extractData(ResultSet rs) throws SQLException {
            Map<Long, Film> filmMap = new LinkedHashMap<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                Film film = filmMap.computeIfAbsent(id, k -> {
                    Film f = new Film();
                    f.setId(k);
                    try {
                        f.setName(rs.getString("name"));
                        f.setDescription(rs.getString("description"));
                        f.setReleaseDate(rs.getDate("release_date").toLocalDate());
                        f.setDuration(rs.getInt("duration"));
                        Integer mpaId = rs.getInt("mpa_id");
                        f.setMpaId(rs.wasNull() ? null : mpaId);
                        f.setGenreIds(new HashSet<>());
                    } catch (SQLException ignored) {
                    }
                    return f;
                });

                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenreIds().add(genreId);
                }
            }
            return filmMap;
        }
    }

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpaId() != null) {
                ps.setInt(5, film.getMpaId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        saveGenresBatch(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId(),
                film.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenresBatch(film);
        return film;
    }

    private void saveGenresBatch(Film film) {
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            List<Object[]> batch = film.getGenreIds().stream()
                    .map(genreId -> new Object[]{film.getId(), genreId})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    batch
            );
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    private void loadAllLikes(List<Film> films) {
        if (films.isEmpty()) return;
        String ids = films.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        Map<Long, Set<Long>> likesMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long userId = ((Number) row.get("user_id")).longValue();
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC, f.id
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            Integer mpaId = rs.getInt("mpa_id");
            film.setMpaId(rs.wasNull() ? null : mpaId);
            film.setGenreIds(new HashSet<>());
            return film;
        }, count);
    }
}
