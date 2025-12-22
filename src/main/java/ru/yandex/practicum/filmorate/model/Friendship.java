package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Long requesterId;
    private Long recipientId;
    private FriendshipStatus status;
    private LocalDateTime created;
    private LocalDateTime confirmed;
}
