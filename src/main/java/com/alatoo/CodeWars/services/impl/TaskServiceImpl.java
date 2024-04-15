package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.entities.Difficulty;
import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.entities.TaskFile;
import com.alatoo.CodeWars.entities.User;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.mappers.TaskMapper;
import com.alatoo.CodeWars.repositories.DifficultyRepository;
import com.alatoo.CodeWars.repositories.TaskFileRepository;
import com.alatoo.CodeWars.repositories.TaskRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.TaskService;
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
import java.util.Locale;
import java.util.Optional;
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final AuthService authService;
    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TaskFileRepository taskFileRepository;
    private final TaskMapper taskMapper;

    @Value("${application.bucket.name}")
    private String bucketName;
    private String path = "http://localhost:8080/file/";

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public String addTask(String token, NewTaskRequest request) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
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
        task.setVerified(true);
        taskRepository.saveAndFlush(task);
        return "The task was added successfully;";
    }

    @Override
    public String addTaskFile(String token, Long id, MultipartFile file) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("404", HttpStatus.NOT_FOUND);
        if(file != null) {
            TaskFile taskFile = saveFile(task.get(), file);
            List<TaskFile> taskFiles = new ArrayList<>();
            taskFiles.add(taskFile);
            task.get().setTaskFiles(taskFiles);
            taskRepository.saveAndFlush(task.get());
        }
        return "Done";
    }

    @Override
    public TaskDetailsResponse showById(String token, Long id) {
        User user = authService.getUserFromToken(token);
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        if(user.getRole().equals(Role.USER) && !task.get().getVerified())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        return taskMapper.taskDetails(task.get());
    }
    @Override
    public List<TaskResponse> showAllTasks(String token){
        User user = authService.getUserFromToken(token);
        return taskMapper.toDtoS();
    }

    @Override
    public List<String> getFileNames( Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Not found", HttpStatus.NOT_FOUND);
        List<String> names = new ArrayList<>();
        for(TaskFile file : task.get().getTaskFiles()){
            names.add(file.getName());
        }
        return names;
    }

    @Override
    public String deleteTaskFiles(Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        List<TaskFile> files = task.get().getTaskFiles();
        task.get().setTaskFiles(null);
        for(TaskFile taskFile : files){
            taskFile.setTask(null);
            taskFileRepository.delete(taskFile);
        }
        taskRepository.saveAndFlush(task.get());
        return "Done";
    }

    @Override
    public byte[] downloadFile(List<String> fileNames) {
        if(fileNames.isEmpty())
            throw new NotFoundException("No files", HttpStatus.NOT_FOUND);
        for(String fileName : fileNames) {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            try {
                byte[] content = IOUtils.toByteArray(inputStream);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
