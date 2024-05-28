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
import com.alatoo.CodeWars.services.TaskFileService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final DifficultyRepository difficultyRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final ReviewRepository reviewRepository;
    private final TaskFileService taskFileService;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TagRepository tagRepository;
    private final MessageSource messageSource;

    @Override
    public String addDifficultyKyu(String token, NewDifficultyRequest request, Locale locale) {

        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        DifficultyKyu difficultyKyu = new DifficultyKyu();
        if(request.getName() == null)
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"name"}));
        difficultyKyu.setName(request.getName().toUpperCase());
        if(request.getPoints() == null)
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"points"}));
        difficultyKyu.setPointsForTask(request.getPoints());
        difficultyKyu.setRequiredPoints(request.getRankUpPoints());
        difficultyRepository.saveAndFlush(difficultyKyu);
        return getMessage("admin.add.diff");
    }

    @Override
    public List<TaskResponse> showAllOffers(String token, Locale locale) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        return taskMapper.newTasksToDtoS();
    }
    @Transactional
    @Override
    public String deleteTask(String token, Long id, Locale locale) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        task.get().setAddedUser(null);
        if(task.get().getDifficulty() != null)
            task.get().getDifficulty().getTasks().remove(task.get());
        task.get().setDifficulty(null);
        for(User user1 : userRepository.findAll()){
            user1.getSolvedTasks().remove(task.get());
        }
        if(task.get().getTaskFiles() != null && !task.get().getTaskFiles().isEmpty())
            taskFileService.deleteTaskFiles(token, id, locale);
        task.get().setAnsweredUsers(null);
        for(User user1 : userRepository.findAll()){
            List<Task> newList = user1.getCreatedTasks();
            newList.remove(task.get());
            user1.setCreatedTasks(newList);
            userRepository.saveAndFlush(user1);
        }
        task.get().setAddedUser(null);
        if(!task.get().getTags().isEmpty()){
            for(Tag tag : tagRepository.findAll()){
                if(task.get().getTags().contains(tag)) {
                    task.get().getTags().remove(tag);
                    tag.getTasks().remove(task.get());
                }
            }
        }
        for(Review review : reviewRepository.findAll()){
            if(review.getTask().equals(task.get())) {
                review.getUser().getReviews().remove(review);
                review.setUser(null);
                task.get().getReviews().remove(review);
                reviewRepository.delete(review);
            }
        }
        for(User user : userRepository.findAll()){
            if(user.getFavorites().contains(task.get())) {
                user.getFavorites().remove(task.get());
                task.get().getMarkedUsers().remove(user);
                userRepository.save(user);
            }
        }
        taskRepository.delete(task.get());
        return getMessage("delete", new Object[]{"Task"});
    }
    @Transactional
    @Override
    public String deleteReview(String token, Long reviewId, Locale locale){
        User admin = authService.getUserFromToken(token);
        if (!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(getMessage("review.not.found"), HttpStatus.NOT_FOUND));
        User user = review.getUser();
        if (user != null) {
            user.getReviews().remove(review);
            userRepository.saveAndFlush(user);
        }
        Task task = review.getTask();
        if (task != null) {
            task.getReviews().remove(review);
            taskRepository.saveAndFlush(task);
        }
        reviewRepository.delete(review);
        return getMessage("review.delete");
    }
    @Override
    public String approveTask(String token, Long taskId, Locale locale) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty() || task.get().getApproved())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        task.get().setApproved(true);
        taskRepository.saveAndFlush(task.get());
        return getMessage("approve");
    }

    @Override
    public String banUser(String token, Long userId, Locale locale) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty() || user.get().getRole().equals(Role.ADMIN) || user.get().getBanned())
            throw new NotFoundException(getMessage("user.not.found"), HttpStatus.NOT_FOUND);
        user.get().setBanned(true);
        String name = user.get().getUsername();
        userRepository.saveAndFlush(user.get());
        return getMessage("ban", new Object[]{name});
    }

    @Override
    public String unbanUser(String token, Long userId, Locale locale) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty() || user.get().getRole().equals(Role.ADMIN) || !user.get().getBanned())
            throw new NotFoundException(getMessage("user.not.found"), HttpStatus.NOT_FOUND);
        user.get().setBanned(false);
        String name = user.get().getUsername();
        userRepository.saveAndFlush(user.get());
        return getMessage("unban", new Object[]{name});
    }

    @Override
    public List<UserResponse> showAllUsers(String token, Locale locale) {
        User admin = authService.getUserFromToken(token);
        if(!admin.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        return userMapper.allUsers();
    }

    @Override
    public String addTags(String token, String tagName, Locale locale) {
        User user = authService.getUserFromToken(token);
        if(!user.getRole().equals(Role.ADMIN))
            throw new BlockedException(getMessage("not.admin"));
        Tag tag = new Tag();
        tag.setName(tagName);
        tag.setTasks(new ArrayList<>());
        tagRepository.save(tag);
        return getMessage("admin.add.tag");
    }
    private String getMessage(String code){
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    private String getMessage(String code, Object[] args){
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
