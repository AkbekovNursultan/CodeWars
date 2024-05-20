package com.alatoo.CodeWars.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String rank;
    private Boolean banned;
    private String createdTasks;
}
