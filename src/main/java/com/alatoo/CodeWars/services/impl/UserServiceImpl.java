package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import com.alatoo.CodeWars.entities.*;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.mappers.UserMapper;
import com.alatoo.CodeWars.repositories.*;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.UserService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final TaskFileRepository taskFileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    @Value("${application.bucket.name}")
    private String bucketName;
    private String path = "http://localhost:8080/file/";

    @Autowired
    private AmazonS3 s3Client;
    @Override
    public UserDtoResponse showUserInfo(String token, Long userId, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<User> user1 = userRepository.findById(userId);
        if(user1.isEmpty())
            throw new NotFoundException(getMessage("user.not.found"), HttpStatus.NOT_FOUND);
        return userMapper.toDto(user1.get());
    }

    @Override
    public String addTaskFile(String token, Long id, MultipartFile file, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(user.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty() || !user.getCreatedTasks().contains(task.get()))
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        if(!task.get().getApproved())
            throw new BadRequestException(getMessage("not.approved"));
        if(file != null) {
            TaskFile taskFile = saveFile(task.get(), file);
            List<TaskFile> taskFiles = new ArrayList<>();
            if(task.get().getTaskFiles().isEmpty())
                taskFiles = task.get().getTaskFiles();
            taskFiles.add(taskFile);
            for(TaskFile taskFile1 : taskFiles){
                taskFileRepository.saveAndFlush(taskFile1);
            }
            task.get().setTaskFiles(taskFiles);
            taskRepository.saveAndFlush(task.get());
        }
        return getMessage("file.add");
    }

    private TaskFile saveFile(Task task , MultipartFile file) {
        TaskFile taskFile = new TaskFile();
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        taskFile.setName(fileName);
        fileObj.delete();

        log.info("File with name = {} has successfully uploaded",taskFile.getName());
        TaskFile taskFile1 = taskFileRepository.saveAndFlush(taskFile);
        String url = "/task/download/"+ task.getId() + "/"+taskFile1.getId();
        taskFile1.setTask(task);
        taskFile1.setPath(url);
        return taskFileRepository.saveAndFlush(taskFile1);
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
