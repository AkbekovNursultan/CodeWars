package com.alatoo.CodeWars.mappers;

import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.entities.Task;

import java.util.List;

public interface TaskMapper {
    List<TaskResponse> newTasksToDtoS();

    TaskDetailsResponse taskDetails(Task task);
}
