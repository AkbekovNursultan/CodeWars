package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

public interface ImageService {
    String upload(String token, MultipartFile file);
//    byte[] downloadFile(String fileName);
    ImageResponse showByUser(String token, Long userId);
    String deleteFile(String token);

    S3Object getFile(String fileName);
}
