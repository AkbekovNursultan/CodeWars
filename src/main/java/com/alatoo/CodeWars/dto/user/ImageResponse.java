package com.alatoo.CodeWars.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponse {
    private Long id;
    private String name;
    private String path;
    private Long userId;
}
