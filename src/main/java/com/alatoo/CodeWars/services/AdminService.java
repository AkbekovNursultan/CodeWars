package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Locale;


public interface AdminService {
    String addDifficultyKyu(String token, NewDifficultyRequest request, Locale locale);

    List<TaskResponse> showAllOffers(String token, Locale locale);

    @Transactional
    String deleteTask(String token, Long task_id, Locale locale);

    String approveTask(String token, Long taskId, Locale locale);

    String banUser(String token, Long userId, Locale locale);

    String unbanUser(String token, Long userId, Locale locale);

    List<UserResponse> showAllUsers(String token, Locale locale);

    String addTags(String token, String tagName, Locale locale);

    String deleteReview(String token, Long reviewId, Locale locale);
}
