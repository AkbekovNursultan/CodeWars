package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import com.alatoo.CodeWars.entities.User;

import java.util.List;

public interface UserMapper {
    UserDtoResponse toDto(User user);

    List<UserResponse> allUsers();
}
