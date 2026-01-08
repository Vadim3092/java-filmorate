package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        return userStorage.save(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId);
        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
