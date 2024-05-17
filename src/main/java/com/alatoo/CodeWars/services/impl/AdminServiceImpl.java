package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import com.alatoo.CodeWars.entities.*;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.BlockedException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.mappers.TaskMapper;
import com.alatoo.CodeWars.mappers.UserMapper;
import com.alatoo.CodeWars.repositories.*;
import com.alatoo.CodeWars.services.AdminService;
import com.alatoo.CodeWars.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final DifficultyRepository difficultyRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final TaskFileRepository taskFileRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TagRepository tagRepository;

    @Override
    public String addDifficulty(String token, NewDifficultyRequest request) {

        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Difficulty difficulty = new Difficulty();
        if(request.getName() == null)
            throw new BadRequestException("Field 'name' must be filled!");
        difficulty.setName(request.getName().toUpperCase());
        if(request.getPoints() == null)
            throw new BadRequestException("Field 'points' must be filled");
        difficulty.setPoints(request.getPoints());
        difficultyRepository.saveAndFlush(difficulty);
        return "New difficulty was added.";
    }

    @Override
    public List<TaskResponse> showAllOffers(String token) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        return taskMapper.newTasksToDtoS();
    }

    @Override
    public String delete(String token, Long id) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        task.get().setAddedUser(null);
        task.get().getDifficulty().getTasks().remove(task.get());
        task.get().setDifficulty(null);
        for(User user1 : userRepository.findAll()){
            user1.getSolvedTasks().remove(task.get());
        }
        task.get().setAnsweredUsers(null);
        List<TaskFile> taskFiles = task.get().getTaskFiles();
        if(!taskFiles.isEmpty()) {
            for (TaskFile file : taskFiles) {
                file.setTask(null);
                taskFileRepository.delete(file);
            }
        }
        for(User user1 : userRepository.findAll()){
            List<Task> newList = user1.getCreatedTasks();
            newList.remove(task.get());
            user1.setCreatedTasks(newList);
            userRepository.saveAndFlush(user1);
        }
        task.get().setAddedUser(null);
        if(!task.get().getTags().isEmpty()){
            for(Tag tag : task.get().getTags()){
                tag.getTasks().remove(task.get());
            }
        }
        taskRepository.delete(task.get());
        return "Successfully deleted.";
    }

    @Override
    public String approveTask(String token, Long taskId) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty() || task.get().getApproved())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        task.get().setApproved(true);
        taskRepository.saveAndFlush(task.get());
        return "Task was successfully approved.";
    }

    @Override
    public String banUser(String token, Long userId) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty() || user.get().getRole().equals(Role.ADMIN) || user.get().getBanned())
            throw new NotFoundException("User not found.", HttpStatus.NOT_FOUND);
        user.get().setBanned(true);
        String name = user.get().getUsername();
        userRepository.saveAndFlush(user.get());
        return "User - "+ name + " was successfully banned.";
    }

    @Override
    public String unbanUser(String token, Long userId) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty() || user.get().getRole().equals(Role.ADMIN) || !user.get().getBanned())
            throw new NotFoundException("User not found.", HttpStatus.NOT_FOUND);
        user.get().setBanned(false);
        String name = user.get().getUsername();
        userRepository.saveAndFlush(user.get());
        return "User - "+ name + " was successfully unbanned.";
    }

    @Override
    public List<UserResponse> showAllUsers(String token) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException("no");
        return userMapper.allUsers();
    }

    @Override
    public String addTags(String token, List<String> tagNames) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException("Nah");
        for(String tagName : tagNames) {
            Tag tag = new Tag();
            tag.setName(tagName);
            tag.setTasks(new ArrayList<>());
            tagRepository.save(tag);
        }
        return "Tag saved";
    }
}
