package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String addTaskFile(String token, Long task_id, MultipartFile file);

    UserDtoResponse showUserInfo(String token, Long userId);
}
