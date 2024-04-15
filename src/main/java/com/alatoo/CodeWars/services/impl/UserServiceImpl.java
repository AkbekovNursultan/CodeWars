package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.entities.Difficulty;
import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.entities.TaskFile;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.repositories.DifficultyRepository;
import com.alatoo.CodeWars.repositories.TaskFileRepository;
import com.alatoo.CodeWars.repositories.TaskRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.UserService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final DifficultyRepository difficultyRepository;
    private final TaskFileRepository taskFileRepository;

    @Value("${application.bucket.name}")
    private String bucketName;
    private String path = "http://localhost:8080/file/";

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public String addTask(String token, NewTaskRequest request) {
        User user = authService.getUserFromToken(token);
        if(user.getBanned())
            throw new BlockedException("BANNED! unlucky m8");
        if(user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        if(request.getName() == null)
            throw new BadRequestException("Field 'name' must be filled!");
        if(request.getDescription() == null)
            throw new BadRequestException("Field 'description' must be filled!");
        Optional<Difficulty> difficulty = difficultyRepository.findByName(request.getDifficulty().toUpperCase(Locale.ROOT));
        if(difficulty.isEmpty())
            throw new BadRequestException("Difficulty type:" + request.getDifficulty() + "doesn't exist!");
        Task task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setDifficulty(difficulty.get());
        task.setAdded_user(user);
        task.setVerified(false);
        taskRepository.saveAndFlush(task);
        return "The task addition was applied.\n It needs to be accepted by admins.";
    }

    @Override
    public String addTaskFile(String token, Long id, MultipartFile file) {
        User user = authService.getUserFromToken(token);
        if(user.getBanned())
            throw new BlockedException("BANNED! unlucky m8");
        if(user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        if(!task.get().getVerified())
            throw new BadRequestException("Task hasn't been accepted yet, Wait.");
        if(file != null) {
            TaskFile taskFile = saveFile(task.get(), file);
            List<TaskFile> taskFiles = new ArrayList<>();
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
        String url = path+taskFile1.getId();
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
}
