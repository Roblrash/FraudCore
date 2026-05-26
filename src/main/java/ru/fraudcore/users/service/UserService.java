package ru.fraudcore.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.fraudcore.common.exception.NotFoundException;
import ru.fraudcore.users.dto.UserResponse;
import ru.fraudcore.users.entity.User;
import ru.fraudcore.users.mapper.UserMapper;
import ru.fraudcore.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getCurrentUser() {
        return userMapper.toResponse(getCurrentUserEntity());
    }

    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Текущий пользователь не найден"));
    }
}
