package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", this::mapRowToUser);
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new ru.yandex.practicum.filmorate.exception.NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);
        user.setId(id);
        return user;
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
        SELECT u.* FROM users u
        JOIN friendship f ON u.id = f.friend_id
        WHERE f.user_id = ? AND f.status = 'PENDING'
        """;
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friendship (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}
