package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.entities.TaskFile;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.repositories.TaskFileRepository;
import com.alatoo.CodeWars.repositories.TaskRepository;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.TaskFileService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskFileServiceImpl implements TaskFileService {
    private final AuthService authService;
    private final TaskRepository taskRepository;
    private final TaskFileRepository taskFileRepository;
    private final UserRepository userRepository;
    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;
    @Override
    public String addTaskFile(String token, Long id, MultipartFile file) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("404", HttpStatus.NOT_FOUND);
        if(file != null) {
            TaskFile taskFile = saveFile(task.get(), file);
            List<TaskFile> taskFiles = new ArrayList<>();
            if(task.get().getTaskFiles().isEmpty())
            taskFiles.add(taskFile);
            task.get().setTaskFiles(taskFiles);
            taskRepository.saveAndFlush(task.get());
        }
        return "Done";
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
        String url = "localhost:8080/task/download/"+ task.getId() + "/"+taskFile1.getId();
        taskFile1.setTask(task);
        taskFile1.setPath(url);
        return taskFileRepository.saveAndFlush(taskFile1);
    }
    @Override
    public String getFileName(Long taskId, Long fileId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task Not Found", HttpStatus.NOT_FOUND);
        String fileName = null;
        for(TaskFile file : task.get().getTaskFiles()){
            if(file.getId().equals(fileId)){
                fileName = file.getName();
            }
        }
        if(fileName == null){
            throw new NotFoundException("File Not Found.", HttpStatus.NOT_FOUND);
        }
        return fileName;
    }
    @Override
    public String deleteTaskFiles(String token, Long taskId) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);

        List<TaskFile> files = taskFileRepository.findByTask(task.get());
        if (files.isEmpty())
            throw new NotFoundException("Files not found!", HttpStatus.NOT_FOUND);

        if (!user.getCreatedTasks().contains(task.get()) && user.getRole() == Role.USER)
            throw new BadRequestException("You have no access.");

        for (TaskFile file : files) {
            s3Client.deleteObject(bucketName, file.getName());
            taskFileRepository.delete(file);
        }
        task.get().setTaskFiles(null);

        taskFileRepository.deleteAll(files);

        userRepository.saveAndFlush(user);
        return "Files successfully removed.";
    }

    @Override
    public byte[] downloadFile(String fileName) {
        if(fileName == null)
            throw new NotFoundException("No files", HttpStatus.NOT_FOUND);
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

}
