package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

public interface TaskService {
    @Transactional
    String addTask(String token, NewTaskRequest request, Locale locale);
    TaskDetailsResponse showById(String token, Long task_id, Locale locale);

    List<TaskResponse> showAllTasks(String token, Locale locale);
;
    String attempt(String token, Long taskId, String answer, Locale locale);

    String getHint(String token, Long taskId, Locale locale);

    List<String> getHints(String token, Long taskId, Locale locale);

    String addReview(String token, Long taskId, ReviewDto reviewDto, Locale locale);

    List<ReviewDto> showAllReviews(String token, Long taskId, Locale locale);

    List<TaskResponse> search(String token, SearchTaskRequest searchRequest, Locale locale);

    List<TaskResponse> showUserTasks(String token, Long userId, Locale locale);

    String markFavorite(String token, Long taskId, Locale locale);

    List<TaskResponse> showFavorites(String token, Locale locale);
}
