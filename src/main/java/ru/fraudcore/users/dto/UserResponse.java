package ru.fraudcore.users.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        LocalDateTime createdAt
) {
}
