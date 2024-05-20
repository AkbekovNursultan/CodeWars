package com.alatoo.CodeWars.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDtoResponse {
    private Long id;
    private String username;
    private String email;
    private String rank;
    private Integer points;
    private ImageResponse image;
    private Integer answeredTasks;
    private String createdTasks;

}
