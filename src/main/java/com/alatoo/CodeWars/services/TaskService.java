package com.alatoo.CodeWars.services;

import com.alatoo.CodeWars.dto.task.*;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TaskService {
    @Transactional
    String addTask(String token, NewTaskRequest request);
    TaskDetailsResponse showById(String token, Long task_id);

    List<TaskResponse> showAllTasks(String token);
;
    String attempt(String token, Long taskId, String answer);

    String getHint(String token, Long taskId);

    List<String> getHints(String token, Long taskId);

    String addReview(String token, Long taskId, ReviewDto reviewDto);

    List<ReviewDto> showAllReviews(String token, Long taskId);

    List<TaskResponse> search(String token, SearchTaskRequest searchRequest);
}
