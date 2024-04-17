package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.alatoo.CodeWars.entities.Image;

public interface ImageMapper {
    ImageResponse toDetailDto(Image image);
}
