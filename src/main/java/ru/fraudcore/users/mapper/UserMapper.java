package ru.fraudcore.users.mapper;

import org.mapstruct.Mapper;
import ru.fraudcore.users.dto.UserResponse;
import ru.fraudcore.users.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
