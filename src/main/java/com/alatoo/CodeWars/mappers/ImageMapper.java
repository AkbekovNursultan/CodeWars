package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.image.ImageResponse;
import com.alatoo.CodeWars.entities.Image;

public interface ImageMapper {
    ImageResponse toDetailDto(Image image);
}
