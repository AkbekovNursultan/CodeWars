package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.mappers.ImageMapper;
import com.alatoo.CodeWars.repositories.ImageRepository;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.ImageService;
import com.alatoo.CodeWars.exceptions.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alatoo.CodeWars.entities.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final AuthService authService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    @Value("${application.bucket.name}")
    private String bucketName;
    private String path = "http://localhost:8080/user/profile/image/";

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public String upload(String token, MultipartFile file, Locale locale) {
        User user = authService.getUserFromToken(token);
        if(user.getRole() != Role.USER)
            throw new BadRequestException(getMessage("not.admin"));
        authService.checkAccess(user);
        if(user.getImage() != null) {
            deleteFile(token, locale);
            imageRepository.deleteById(user.getImage().getId());
        }
        Image image = saveImage(file);
        user.setImage(image);
        image.setUser(user);
        imageRepository.save(image);
        userRepository.saveAndFlush(user);
        return getMessage("file.add");
    }

    private Image saveImage(MultipartFile file) {
        Image image = new Image();

        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        image.setName(fileName);
        fileObj.delete();

        log.info("File with name = {} has successfully uploaded", image.getName());
        Image image1 = imageRepository.saveAndFlush(image);
        String url = path+image1.getName();
        image1.setPath(url);
        return imageRepository.saveAndFlush(image1);
    }

//    @Override
//    public byte[] downloadFile(String fileName) {
//        S3Object s3Object = s3Client.getObject(bucketName, fileName);
//        S3ObjectInputStream inputStream = s3Object.getObjectContent();
//        try {
//            byte[] content = IOUtils.toByteArray(inputStream);
//            return content;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    @Override
    public String deleteFile(String token, Locale locale) {
        User user = authService.getUserFromToken(token);
        if(user.getRole() != Role.USER)
            throw new BadRequestException(getMessage("not.admin"));
        authService.checkAccess(user);
        Optional<Image> image = imageRepository.findByUser(user);
        System.out.println(image);
        if(image.isEmpty())
            throw new NotFoundException(getMessage("image.not.found"), HttpStatus.NOT_FOUND);
        String fileName = image.get().getName();
        user.setImage(null);
        image.get().setUser(null);
        userRepository.saveAndFlush(user);
        imageRepository.delete(image.get());
        s3Client.deleteObject(bucketName, fileName);
        return getMessage("files.delete");
    }

    @Override
    public S3Object getFile(String token, String fileName) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return s3Client.getObject(bucketName, fileName);
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
    private String getMessage(String code){
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    private String getMessage(String code, Object[] args){
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
