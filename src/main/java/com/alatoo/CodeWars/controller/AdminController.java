package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import com.alatoo.CodeWars.services.AdminService;
import com.alatoo.CodeWars.services.TaskFileService;
import com.alatoo.CodeWars.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final TaskService taskService;
    private final TaskFileService taskFileService;
    @PostMapping("/add/difficulty")
    public String addDifficulty(@RequestHeader("Authorization") String token, @RequestBody NewDifficultyRequest request,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.addDifficultyKyu(token, request, locale);
    }
    @PostMapping("/add/tags")
    private String addTag(@RequestHeader("Authorization") String token, @RequestParam String tagName,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.addTags(token, tagName, locale);
    }
    @PostMapping("/add/task")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.addTask(token, request, locale);
    }
    @PostMapping("/add/{task_id}")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskFileService.addTaskFile(token, task_id, file, locale);
    }
    @DeleteMapping("/task/{task_id}/delete_files")
    public String deleteFiles(@RequestHeader("Authorization") String token, @PathVariable Long task_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskFileService.deleteTaskFiles(token, task_id, locale);
    }
    @GetMapping("/show/task_offers")
    public List<TaskResponse> showAllOffers(@RequestHeader("Authorization") String token, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.showAllOffers(token, locale);
    }
    @PostMapping("/task/{task_id}/approve")
    public String approveTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.approveTask(token, task_id, locale);
    }
    @DeleteMapping("/task/{task_id}/delete")
    public String deleteTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.deleteTask(token, task_id, locale);
    }
    @DeleteMapping("/review/{review_id}/delete")
    public String deleteReview(@RequestHeader("Authorization") String token, @PathVariable Long review_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.deleteReview(token, review_id, locale);
    }
    @GetMapping("/all_users")
    public List<UserResponse> allUsers(@RequestHeader("Authorization") String token, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.showAllUsers(token, locale);
    }
    @PutMapping("/{user_id}/ban")
    public String banUser(@RequestHeader("Authorization") String token, @PathVariable Long user_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.banUser(token, user_id, locale);
    }
    @PutMapping("/{user_id}/unban")
    public String unbanUser(@RequestHeader("Authorization") String token, @PathVariable Long user_id, @RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return adminService.unbanUser(token, user_id, locale);
    }
}
