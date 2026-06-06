package ru.fraudcore.auth.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.fraudcore.auth.dto.AuthResponse;
import ru.fraudcore.auth.dto.LoginRequest;
import ru.fraudcore.auth.dto.RegisterRequest;
import ru.fraudcore.auth.service.AuthService;
import ru.fraudcore.users.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
