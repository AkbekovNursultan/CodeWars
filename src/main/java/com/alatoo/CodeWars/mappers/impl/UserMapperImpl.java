package com.alatoo.CodeWars.mappers.impl;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.mappers.UserMapper;
import com.alatoo.CodeWars.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {
    private final UserRepository userRepository;
    @Override
    public UserDtoResponse toDto(User user) {
        UserDtoResponse response = new UserDtoResponse();
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
        response.setAnsweredTasks(user.getSolvedTasks().size());
        response.setCreatedTasks(user.getCreatedTasks().size());
        return response;
    }

    @Override
    public List<UserResponse> allUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for(User user : users){
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setRank(user.getRank());
            response.setBanned(user.getBanned());
            responses.add(response);
        }
        return responses;
    }
}
