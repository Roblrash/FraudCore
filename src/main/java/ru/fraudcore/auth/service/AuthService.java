package ru.fraudcore.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.auth.dto.AuthResponse;
import ru.fraudcore.auth.dto.LoginRequest;
import ru.fraudcore.auth.dto.RegisterRequest;
import ru.fraudcore.auth.security.JwtService;
import ru.fraudcore.common.exception.ConflictException;
import ru.fraudcore.common.exception.UnauthorizedException;
import ru.fraudcore.users.dto.UserResponse;
import ru.fraudcore.users.entity.User;
import ru.fraudcore.users.mapper.UserMapper;
import ru.fraudcore.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Неверные учетные данные");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Неверные учетные данные"));

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, userMapper.toResponse(user));
    }
}
