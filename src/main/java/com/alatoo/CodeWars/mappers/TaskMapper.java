package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.task.ReviewDto;
import com.alatoo.CodeWars.dto.task.SearchTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.entities.User;

import java.util.List;

public interface TaskMapper {
    List<TaskResponse> newTasksToDtoS();

    TaskDetailsResponse taskDetails(Task task);

    List<TaskResponse> toDtoS();
    List<TaskResponse> toDtoS(User user, SearchTaskRequest request);

    List<ReviewDto> allReviews(Task task);
}
