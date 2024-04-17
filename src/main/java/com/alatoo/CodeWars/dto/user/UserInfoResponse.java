package com.alatoo.CodeWars.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private Integer rank;
    private Integer points;
    private ImageResponse image;
    private Integer answeredTasks;
    private Integer createdTasks;

}
