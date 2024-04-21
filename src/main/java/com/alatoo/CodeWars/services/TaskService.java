package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {
    String addTask(String token, NewTaskRequest request);

    String addTaskFile(String token, Long task_id, MultipartFile file);
    byte[] downloadFile(String fileName);

    TaskDetailsResponse showById(String token, Long task_id);

    List<TaskResponse> showAllTasks(String token);

    String getFileName(Long taskId, Long fileId);
    String attempt(String token, Long taskId, String answer);

    String getHint(String token, Long taskId);

    List<String> getHints(String token, Long taskId);
}
