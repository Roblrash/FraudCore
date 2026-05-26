package ru.fraudcore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Пароль должен содержать минимум 8 символов") String password,
        @NotBlank String fullName
) {
}
