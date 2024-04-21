package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String addTask(String token, NewTaskRequest request);

    String addTaskFile(String token, Long task_id, MultipartFile file);

    UserDtoResponse showUserInfo(String token, Long userId);

    String deleteTaskFiles(Long taskId);
}
