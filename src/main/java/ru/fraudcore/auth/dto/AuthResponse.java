package ru.fraudcore.auth.dto;

import ru.fraudcore.users.dto.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
