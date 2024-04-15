package com.alatoo.CodeWars.mappers.impl;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.mappers.TaskMapper;
import com.alatoo.CodeWars.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMapperImpl implements TaskMapper {
    private final TaskRepository taskRepository;
    @Override
    public List<TaskResponse> newTasksToDtoS() {
        List<Task> allTasks = taskRepository.findAll();
        List<TaskResponse> newTasks = new ArrayList<>();
        for(Task task : allTasks){
            if(!task.getVerified()){
                TaskResponse response = new TaskResponse();
                response.setId(task.getId());
                response.setName(task.getName());
                response.setDifficulty(task.getDifficulty().getName());
                newTasks.add(response);
            }
        }
        return newTasks;
    }

    @Override
    public TaskDetailsResponse taskDetails(Task task) {
        TaskDetailsResponse response = new TaskDetailsResponse();
        response.setId(task.getId());
        response.setName(task.getDifficulty().getName());
        response.setDescription(task.getDescription());
        response.setTaskFiles(task.getTaskFiles());
        response.setPoints(task.getDifficulty().getPoints());
        response.setSolved(task.getAnswered_users().size());
        response.setVerified(task.getVerified());
        return response;
    }

    @Override
    public List<TaskResponse> toDtoS() {
        List<TaskResponse> responseList = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();
        for(Task task : allTasks){
            TaskResponse response = new TaskResponse();
            response.setId(task.getId());
            response.setName(task.getName());
            response.setDifficulty(task.getDifficulty().getName());
            responseList.add(response);
        }
        return responseList;
    }
}
