package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testSaveAndFindUser() {
        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("testlogin");
        user.setName("Test");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User saved = userStorage.save(user);
        User found = userStorage.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@test.ru");
    }
}
