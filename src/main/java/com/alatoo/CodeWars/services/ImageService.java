package com.alatoo.CodeWars.services;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public interface ImageService {
    String upload(String token, MultipartFile file, Locale locale);
//    byte[] downloadFile(String fileName);
    String deleteFile(String token, Locale locale);

    S3Object getFile(String token, String fileName);
}
