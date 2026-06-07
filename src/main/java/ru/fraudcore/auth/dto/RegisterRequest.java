package ru.fraudcore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 255, message = "Пароль должен содержать от 8 до 255 символов") String password,
        @NotBlank @Size(max = 255) String fullName
) {
}
