package com.alatoo.CodeWars.services;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(String token, MultipartFile file);
//    byte[] downloadFile(String fileName);
    String deleteFile(String token);

    S3Object getFile(String token, String fileName);
}
