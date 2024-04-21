package com.alatoo.CodeWars.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Integer rank;
    private Boolean banned;
}
