package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface TaskService {
    String addTask(String token, NewTaskRequest request);

    String addTaskFile(String token, Long task_id, MultipartFile file);

    TaskDetailsResponse showById(String token, Long task_id);
}
