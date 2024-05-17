package com.alatoo.CodeWars.services.impl;

import com.alatoo.CodeWars.dto.task.*;
import com.alatoo.CodeWars.entities.*;
import com.alatoo.CodeWars.enums.Role;
import com.alatoo.CodeWars.exceptions.BadRequestException;
import com.alatoo.CodeWars.exceptions.NotFoundException;
import com.alatoo.CodeWars.mappers.TaskMapper;
import com.alatoo.CodeWars.repositories.*;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final AuthService authService;
    private final TaskRepository taskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final HintRepository hintRepository;
    private final TagRepository tagRepository;

    @Override
    public String addTask(String token, NewTaskRequest request) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        String message = "The task addition was applied.\n It needs to be accepted by admins.";
        if(request.getName() == null)
            throw new BadRequestException("Field 'name' must be filled!");
        if(request.getDescription() == null)
            throw new BadRequestException("Field 'description' must be filled!");
        if(request.getAnswer() == null || request.getAnswer().trim().equals(""))
            throw new BadRequestException("Parameter 'Answer' mustn't be empty.");
        Optional<Difficulty> difficulty = difficultyRepository.findByName(request.getDifficulty().toUpperCase());
        if(difficulty.isEmpty())
            throw new BadRequestException("Difficulty type:" + request.getDifficulty() + "doesn't exist!");
        if(request.getHints().size() > 3)
            throw new BadRequestException("Max number of hints is 3.");
        Task task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setDifficulty(difficulty.get());
        task.setAddedUser(user);
        task.setApproved(false);
        if(user.getRole().equals(Role.ADMIN)) {
            message = "The task was added successfully";
            task.setApproved(true);
        }
        task.setAnswer(request.getAnswer());
        List<Hint> newHints = new ArrayList<>();
        for(String text : request.getHints()){
            Hint hint = new Hint();
            hint.setTask(task);
            hint.setHint(text);
            newHints.add(hint);
            taskRepository.save(task);
            hintRepository.saveAndFlush(hint);
        }
        List<Tag> taskTags = new ArrayList<>();
        for(String requestTag : request.getTags()){
            List<Tag> allTags = tagRepository.findAll();
            for(Tag tag : allTags){
                if(tag.getName().equalsIgnoreCase(requestTag)){
                    taskTags.add(tag);
                    tag.getTasks().add(task);
                }
            }
        }

        task.setTags(taskTags);
        task.setTaskFiles(new ArrayList<>());
        task.setReviews(new ArrayList<>());
        task.setHints(newHints);
        task.setCreatedDate(LocalDateTime.now());
        taskRepository.saveAndFlush(task);
        difficulty.get().getTasks().add(task);
        //--
        user.getCreatedTasks().add(task);
        userRepository.save(user);
        return message;
    }

    @Override
    public TaskDetailsResponse showById(String token, Long id) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        if(user.getRole().equals(Role.USER) && !task.get().getApproved())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        return taskMapper.taskDetails(task.get());
    }
    @Override
    public List<TaskResponse> showAllTasks(String token){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return taskMapper.toDtoS();
    }
    @Override
    public List<TaskResponse> search(String token, SearchTaskRequest request){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return taskMapper.toDtoS(user, request);
    }

    @Override
    public String attempt(String token, Long taskId, String answer) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        if(user.getCreatedTasks().contains(task.get()))
            throw new BadRequestException("You can't answer to your own task");
        if(user.getSolvedTasks().contains(task.get()))
            throw new BadRequestException("You have already done this task");
        if(task.get().getAnswer().equals(answer)){
            List<Task> tasks = new ArrayList<>();
            if(!user.getSolvedTasks().isEmpty())
               tasks = user.getSolvedTasks();
            tasks.add(task.get());
            List<Hint> hints = task.get().getHints();
            List<String> usedHints = new ArrayList<>();
            for(Hint hint : hints){
                if(hint.getReceivedUsers().contains(user))
                    usedHints.add(hint.getHint());
            }
            int earnedPoints = task.get().getDifficulty().getPoints() - task.get().getDifficulty().getPoints() * usedHints.size() / 10;
            user.setPoints(user.getPoints() + earnedPoints);
            String message = "Congratulations! You have found a correct answer!\n" + earnedPoints + " points earned";
            int previousRank = user.getRank();
            user.setRank(user.getPoints()/1000);
            if(previousRank < user.getRank())
                message = message + "\nYour rank has been increased!\n" + previousRank + "--->" + user.getRank();
            user.setSolvedTasks(tasks);
            List<User> users = new ArrayList<>();
            if(!task.get().getAnsweredUsers().isEmpty())
                users = task.get().getAnsweredUsers();
            task.get().setAnsweredUsers(users);
            taskRepository.saveAndFlush(task.get());
            userRepository.saveAndFlush(user);
            return message;
        }

        return "Incorrect answer.\nTry again.";
    }

    @Override
    public String getHint(String token, Long taskId) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(user.getRole().equals(Role.ADMIN))
            throw new BadRequestException("Why?");
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        List<Hint> hints = new ArrayList<>();
        for(Hint hint : task.get().getHints()){
            if(!hint.getReceivedUsers().contains(user))
                hints.add(hint);
        }
        if(hints.isEmpty())
            return "You have run out of hints.";
        List<User> receivedUsers = hints.get(0).getReceivedUsers();
        receivedUsers.add(user);
        hints.get(0).setReceivedUsers(receivedUsers);
        hintRepository.save(hints.get(0));
        return hints.get(0).getHint();
    }

    @Override
    public List<String> getHints(String token, Long taskId) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(user.getRole().equals(Role.ADMIN))
            throw new BadRequestException("Why?");
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        List<String> hints = new ArrayList<>();
        for(Hint hint : task.get().getHints()){
            if(hint.getReceivedUsers().contains(user))
                hints.add(hint.getHint());
        }
        return hints;
    }

    @Override
    public String addReview(String token, Long taskId, ReviewDto reviewDto) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty() || user.getCreatedTasks().contains(task.get()))
            throw new NotFoundException("Task not found.", HttpStatus.NOT_FOUND);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        Review review = new Review();
        review.setText(reviewDto.getText());
        review.setRating(reviewDto.getRating());
        review.setTask(task.get());
        review.setUser(user);
        task.get().getReviews().add(review);
        task.get().setRating(Double.parseDouble(decimalFormat.format(task.get().getRating() / task.get().getReviews().size())));
        taskRepository.save(task.get());
        return "Done";
    }

    @Override
    public List<ReviewDto> showAllReviews(String token, Long taskId) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException("Task not found", HttpStatus.NOT_FOUND);
        return taskMapper.allReviews(task.get());
    }
}
