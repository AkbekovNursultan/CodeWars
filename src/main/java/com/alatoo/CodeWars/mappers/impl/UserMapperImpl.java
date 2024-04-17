package com.alatoo.CodeWars.mappers.impl;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.alatoo.CodeWars.dto.user.UserInfoResponse;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.mappers.UserMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {
    @Override
    public UserInfoResponse toDto(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        ImageResponse image = new ImageResponse();
        image.setName(user.getImage().getName());
        image.setPath(user.getImage().getPath());
        image.setId(user.getImage().getId());
        image.setUserId(user.getId());
        response.setImage(image);
        response.setPoints(user.getPoints());
        response.setRank(user.getRank());
        response.setAnsweredTasks(user.getAnsweredTasks().size());
        response.setCreatedTasks(user.getCreatedTasks().size());
        return response;
    }
}
