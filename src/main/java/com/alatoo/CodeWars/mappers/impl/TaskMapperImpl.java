package com.alatoo.CodeWars.mappers.impl;

import com.alatoo.CodeWars.dto.task.*;
import com.alatoo.CodeWars.entities.*;
import com.alatoo.CodeWars.mappers.TaskMapper;
import com.alatoo.CodeWars.repositories.DifficultyRepository;
import com.alatoo.CodeWars.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMapperImpl implements TaskMapper {
    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    @Override
    public List<TaskResponse> newTasksToDtoS() {
        List<Task> allTasks = taskRepository.findAll();
        List<TaskResponse> responseList = new ArrayList<>();
        for(Task task : allTasks){
            if(!task.getApproved()){
                createTaskResponse(responseList, task);
            }
        }
        return responseList;
    }
    @Override
    public List<TaskResponse> showFavorites(User user){
        List<TaskResponse> responseList = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();
        for(Task task : allTasks){
            if(!task.getApproved())
                continue;
            if(!user.getFavorites().contains(task))
                continue;
            createTaskResponse(responseList, task);
        }
        return responseList;
    }

    private void createTaskResponse(List<TaskResponse> responseList, Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDifficulty(task.getDifficulty().getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        response.setCreatedDate(task.getCreatedDate().format(formatter));
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        response.setRating(decimalFormat.format(task.getRating()));
        response.setSolved(task.getSolved());
        response.setTags(new ArrayList<>());
        for(Tag tag : task.getTags()) {
            response.getTags().add(tag.getName());
        }
        responseList.add(response);
    }

    @Override
    public TaskDetailsResponse taskDetails(Task task) {
        TaskDetailsResponse response = new TaskDetailsResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setDifficulty(task.getDifficulty().getName());
        List<TaskFileDtoResponse> taskFileDtoResponses = new ArrayList<>();
        for(TaskFile taskFile : task.getTaskFiles()){
            TaskFileDtoResponse taskFileDtoResponse = new TaskFileDtoResponse();
            taskFileDtoResponse.setId(taskFile.getId());
            taskFileDtoResponse.setName(taskFile.getName());
            taskFileDtoResponse.setPath(taskFile.getPath());
            taskFileDtoResponses.add(taskFileDtoResponse);
        }
        response.setTaskFiles(taskFileDtoResponses);
        List<String> tags = new ArrayList<>();
        for(Tag tag : task.getTags()){
            tags.add(tag.getName());
        }
        response.setTags(tags);
        response.setPoints(task.getDifficulty().getPointsForTask());
        response.setSolved(task.getAnsweredUsers().size());
        response.setCreatedBy(task.getAddedUser().getUsername());
        response.setVerified(task.getApproved());
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        response.setRating(decimalFormat.format(task.getRating()));
        return response;
    }

    @Override
    public List<TaskResponse> toDtoS() {
        List<TaskResponse> responseList = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();
        for(Task task : allTasks){
            if(!task.getApproved())
                continue;
            createTaskResponse(responseList, task);
        }
        return responseList;
    }
    @Override
    public List<TaskResponse> toDtoS(User user) {
        List<TaskResponse> responseList = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();
        for(Task task : allTasks){
            if(!task.getApproved())
                continue;
            if(!task.getAddedUser().equals(user))
                continue;
            createTaskResponse(responseList, task);
        }
        return responseList;
    }
    @Override
    public List<TaskResponse> toDtoS(User user, SearchTaskRequest request){
        List<TaskResponse> responseList = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();
        if(request.getSortBy() != null && !request.getSortBy().isBlank())
            allTasks = sortTasks(request.getSortBy());
        for(Task task : allTasks){
            if(!task.getApproved())
                continue;
            if(filter(request, task, user)){
                TaskResponse response = new TaskResponse();
                response.setName(task.getName());
                response.setId(task.getId());
                response.setDifficulty(task.getDifficulty().getName());
                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                response.setRating(decimalFormat.format(task.getRating()));
                response.setTags(new ArrayList<>());
                for(Tag tag : task.getTags()) {
                    response.getTags().add(tag.getName());
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                response.setCreatedDate(task.getCreatedDate().format(formatter));
                response.setSolved(task.getSolved());
                responseList.add(response);
            }
        }
        return responseList;
    }
    private boolean filter(SearchTaskRequest request, Task task, User user){
        if(request.getName() != null && !request.getName().isBlank()){
                if(!task.getName().toUpperCase().strip().contains(request.getName().toUpperCase().strip()))
                    return false;
        }
        if(request.getDifficulty() != null && !request.getDifficulty().isBlank()) {
            if(!request.getDifficulty().equalsIgnoreCase(task.getDifficulty().getName()))
                return false;
        }
        List<String> tagNames = new ArrayList<>();
        for(Tag tag : task.getTags()){
            tagNames.add(tag.getName());
        }
        if(request.getTags() != null && !request.getTags().isEmpty()){
            if(task.getTags().size() < request.getTags().size())
                return false;
            for(String name : request.getTags()){
                if(!tagNames.contains(name))
                    return false;
            }
        }
        if(request.getIsSolved() != null){
            if(!request.getIsSolved()){
                if(task.getAnsweredUsers().contains(user))
                    return false;
            }
        }
        return true;
    }
    private List<Task> sortTasks(String sortBy){
        if(sortBy.equalsIgnoreCase("newest")){
            Sort sortByCreatedDateDesc = Sort.by(Sort.Direction.DESC, "createdDate");
            return taskRepository.findAll(sortByCreatedDateDesc);
        }
        if(sortBy.equalsIgnoreCase("oldest")){
            Sort sortByCreatedDateAsc = Sort.by(Sort.Direction.ASC, "createdDate");
            return taskRepository.findAll(sortByCreatedDateAsc);
        }
        if(sortBy.equalsIgnoreCase("popular")){
            Sort sortByPopularity = Sort.by(Sort.Direction.DESC, "solved");
            return taskRepository.findAll(sortByPopularity);
        }
        if(sortBy.equalsIgnoreCase("unpopular")){
            Sort sortByPopularity = Sort.by(Sort.Direction.ASC, "solved");
            return taskRepository.findAll(sortByPopularity);
        }
        if(sortBy.equalsIgnoreCase("highest")){
            Sort sortByRating = Sort.by(Sort.Direction.DESC, "rating");
            return taskRepository.findAll(sortByRating);
        }
        return taskRepository.findAll();
    }

    @Override
    public List<ReviewDto> allReviews(Task task) {
        List<ReviewDto> responses = new ArrayList<>();
        for(Review review : task.getReviews()){
            ReviewDto response = new ReviewDto();
            response.setId(review.getId());
            response.setAuthor(review.getUser().getUsername());
            response.setRating(review.getRating());
            response.setText(review.getText());
            responses.add(response);
        }
        return responses;
    }
}
