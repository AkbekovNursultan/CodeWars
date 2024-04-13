package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String addTask(String token, NewTaskRequest request);

    String addTaskFile(String token, Long task_id, MultipartFile file);
}