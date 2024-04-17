package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.user.UserInfoResponse;
import com.alatoo.CodeWars.entities.User;

public interface UserMapper {
    UserInfoResponse toDto(User user);
}
