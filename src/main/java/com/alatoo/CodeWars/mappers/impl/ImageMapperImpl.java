package com.alatoo.CodeWars.mappers.impl;

import com.alatoo.CodeWars.dto.image.ImageResponse;
import com.alatoo.CodeWars.entities.Image;
import com.alatoo.CodeWars.mappers.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageMapperImpl implements ImageMapper {
    @Override
    public ImageResponse toDetailDto(Image image) {
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setPath(image.getPath());
        response.setName(image.getName());
        response.setUserId(image.getUser().getId());
        return response;
    }
}
