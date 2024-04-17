package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(String token, MultipartFile file);
//    byte[] downloadFile(String fileName);
    ImageResponse showByUser(String token, Long userId);
    String deleteFile(String token);
}
