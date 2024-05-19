package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import jakarta.transaction.Transactional;

import java.util.List;


public interface AdminService {
    String addDifficulty(String token, NewDifficultyRequest request);

    List<TaskResponse> showAllOffers(String token);

    @Transactional
    String delete(String token, Long task_id);

    String approveTask(String token, Long taskId);

    String banUser(String token, Long userId);

    String unbanUser(String token, Long userId);

    List<UserResponse> showAllUsers(String token);

    String addTags(String token, String tagName);
}
