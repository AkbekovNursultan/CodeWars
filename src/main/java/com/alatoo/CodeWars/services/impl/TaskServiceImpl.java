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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final ReviewRepository reviewRepository;
    private final MessageSource messageSource;
    @Override
    public String addTask(String token, NewTaskRequest request, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        String message = getMessage("user.add.task");
        if(request.getName() == null)
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"'name'"}));
        if(request.getDescription() == null)
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"'description'"}));
        if(request.getAnswer() == null || request.getAnswer().trim().equals(""))
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"'answer'"}));
        if(request.getDifficulty() == null || request.getDifficulty().trim().equals(""))
            throw new BadRequestException(getMessage("add.empty.error", new Object[]{"'difficulty'"}));
        Optional<DifficultyKyu> difficulty = difficultyRepository.findByName(request.getDifficulty().toUpperCase());
        if(difficulty.isEmpty())
            throw new BadRequestException(getMessage("difficulty.not.found"));
        if(request.getHints().size() > 3)
            throw new BadRequestException(getMessage("hints.max"));
        Task task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setDifficulty(difficulty.get());
        task.setAddedUser(user);
        task.setApproved(false);
        if(user.getRole().equals(Role.ADMIN)) {
            message = getMessage("admin.add.task");
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
        if(request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> taskTags = new ArrayList<>();
            for (String requestTag : request.getTags()) {
                List<Tag> allTags = tagRepository.findAll();
                for (Tag tag : allTags) {
                    if (tag.getName().equalsIgnoreCase(requestTag)) {
                        taskTags.add(tag);
                        tag.getTasks().add(task);
                    }
                }
            }
            task.setTags(taskTags);
        }
        task.setTaskFiles(new ArrayList<>());
        task.setReviews(new ArrayList<>());
        task.setMarkedUsers(new ArrayList<>());
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
    public TaskDetailsResponse showById(String token, Long id, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(id);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        if(user.getRole().equals(Role.USER) && !task.get().getApproved())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        return taskMapper.taskDetails(task.get());
    }
    @Override
    public List<TaskResponse> showAllTasks(String token, Locale locale){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return taskMapper.toDtoS();
    }
    @Override
    public List<TaskResponse> showUserTasks(String token, Long userId, Locale locale){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<User> user1 = userRepository.findById(userId);
        if(user1.isEmpty())
            throw new NotFoundException(getMessage("user.not.found"), HttpStatus.NOT_FOUND);
        return taskMapper.toDtoS(user1.get());
    }
    @Override
    public String markFavorite(String token, Long taskId, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        String message = getMessage("task.mark");
        if (task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        if (!user.getFavorites().contains(task.get())){
            user.getFavorites().add(task.get());
            task.get().getMarkedUsers().add(user);
        }else {
            user.getFavorites().remove(task.get());
            task.get().getMarkedUsers().remove(user);
            message = getMessage("task.unmark");
        }
        taskRepository.save(task.get());
        userRepository.save(user);
        return message;
    }
    @Override
    public List<TaskResponse> showFavorites(String token, Locale locale){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return taskMapper.showFavorites(user);
    }
    @Override
    public List<TaskResponse> search(String token, SearchTaskRequest request, Locale locale){
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        return taskMapper.toDtoS(user, request);
    }

    @Override
    public String attempt(String token, Long taskId, String answer, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        if(user.getCreatedTasks().contains(task.get()))
            throw new BadRequestException(getMessage("task.error1"));
        if(user.getSolvedTasks().contains(task.get()))
            throw new BadRequestException(getMessage("task.error2"));
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
            int earnedPoints = task.get().getDifficulty().getPointsForTask() - task.get().getDifficulty().getPointsForTask() * usedHints.size() / 10;
            String message = getMessage("attempt.success", new Object[]{earnedPoints});
            String previousRank = user.getRank();
            if(didUserRankUp(user, earnedPoints))
                message = message + getMessage("rankUp", new Object[]{previousRank, user.getRank()});
            user.setSolvedTasks(tasks);
            List<User> users = new ArrayList<>();
            if(!task.get().getAnsweredUsers().isEmpty())
                users = task.get().getAnsweredUsers();
            users.add(user);
            task.get().setAnsweredUsers(users);
            task.get().setSolved(task.get().getSolved() + 1);
            taskRepository.saveAndFlush(task.get());
            userRepository.saveAndFlush(user);
            return message;
        }
        return getMessage("fail");
    }
    private Boolean didUserRankUp(User user, int earnedPoints){
        Sort sort = Sort.by(Sort.Direction.ASC, "requiredPoints");
        List<DifficultyKyu> allRanks = difficultyRepository.findAll(sort);
        String currentRank = "Beginner";
        for(DifficultyKyu rank : allRanks){
            if(rank.getRequiredPoints() <= user.getPoints())
                currentRank = rank.getName();
        }
        String newRank = currentRank;
        user.setPoints(user.getPoints() + earnedPoints);
        for(DifficultyKyu rank : allRanks){
            if(rank.getRequiredPoints() <= user.getPoints())
                newRank = rank.getName();
        }
        if(currentRank.equals(newRank))
            return false;
        user.setRank(newRank);
        return true;
    }
    @Override
    public String getHint(String token, Long taskId, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(user.getRole().equals(Role.ADMIN))
            throw new BadRequestException(getMessage("not.admin"));
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        List<Hint> hints = new ArrayList<>();
        for(Hint hint : task.get().getHints()){
            if(!hint.getReceivedUsers().contains(user))
                hints.add(hint);
        }
        if(hints.isEmpty())
            return getMessage("no.hints");
        List<User> receivedUsers = hints.get(0).getReceivedUsers();
        receivedUsers.add(user);
        hints.get(0).setReceivedUsers(receivedUsers);
        hintRepository.save(hints.get(0));
        return hints.get(0).getHint();
    }

    @Override
    public List<String> getHints(String token, Long taskId, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        if(user.getRole().equals(Role.ADMIN))
            throw new BadRequestException(getMessage("not.admin"));
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        List<String> hints = new ArrayList<>();
        for(Hint hint : task.get().getHints()){
            if(hint.getReceivedUsers().contains(user))
                hints.add(hint.getHint());
        }
        return hints;
    }

    @Override
    public String addReview(String token, Long taskId, ReviewDto reviewDto, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        if(user.getCreatedTasks().contains(task.get()))
            throw new BadRequestException(getMessage("task.error3"));
        if(doesReviewExist(task.get(), user)) {
            List<Review> reviewList = reviewRepository.findAll();
            for(Review review : reviewList){
                if(review.getUser() == user) {
                    review.getUser().getReviews().remove(review);
                    review.setUser(null);
                    task.get().getReviews().remove(review);
                    reviewRepository.delete(review);
                }
            }
        }
        Review review = new Review();
        review.setText(reviewDto.getText());
        if(reviewDto.getRating() > 5.0)
            reviewDto.setRating(5.0);
        if(reviewDto.getRating() < 0.0)
            reviewDto.setRating(0.0);
        review.setRating(reviewDto.getRating());
        review.setTask(task.get());
        review.setUser(user);
        reviewRepository.save(review);
        task.get().getReviews().add(review);
        user.getReviews().add(review);
        task.get().setRating(calculateRating(taskRepository.save(task.get())));
        userRepository.save(user);
        taskRepository.save(task.get());
        return getMessage("review.add");
    }
    private Double calculateRating(Task task){
        Double result = 0.0;
        for(Review review : task.getReviews()){
            result += review.getRating();
        }
        result /= task.getReviews().size();
        return result;
    }
    private Boolean doesReviewExist(Task task, User user){
        for(Review review : task.getReviews()){
            if(review.getUser() == user)
                return true;
        }
        return false;
    }

    @Override
    public List<ReviewDto> showAllReviews(String token, Long taskId, Locale locale) {
        User user = authService.getUserFromToken(token);
        authService.checkAccess(user);
        Optional<Task> task = taskRepository.findById(taskId);
        if(task.isEmpty())
            throw new NotFoundException(getMessage("task.not.found"), HttpStatus.NOT_FOUND);
        return taskMapper.allReviews(task.get());
    }
    private String getMessage(String code){
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    private String getMessage(String code, Object[] args){
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
