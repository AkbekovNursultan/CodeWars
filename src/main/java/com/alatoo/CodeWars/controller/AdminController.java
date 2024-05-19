package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserResponse;
import com.alatoo.CodeWars.services.AdminService;
import com.alatoo.CodeWars.services.TaskFileService;
import com.alatoo.CodeWars.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final TaskService taskService;
    private final TaskFileService taskFileService;
    @PostMapping("/add/difficulty")
    public String addDifficulty(@RequestHeader("Authorization") String token, @RequestBody NewDifficultyRequest request){
        return adminService.addDifficulty(token, request);
    }
    @PostMapping("/add/tags")
    private String addTag(@RequestHeader("Authorization") String token, @RequestParam String tagName){
        return adminService.addTags(token, tagName);
    }
    @PostMapping("/add/task")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request){
        return taskService.addTask(token, request);
    }
    @PostMapping("/add/{task_id}")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return taskFileService.addTaskFile(token, task_id, file);
    }
    @DeleteMapping("/task/{task_id}/delete_files")
    public String deleteFiles(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskFileService.deleteTaskFiles(token, task_id);
    }
    @GetMapping("/show/task_offers")
    public List<TaskResponse> showAllOffers(@RequestHeader("Authorization") String token){
        return adminService.showAllOffers(token);
    }
    @PostMapping("/approve/{task_id}")
    public String approveTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return adminService.approveTask(token, task_id);
    }
    @DeleteMapping("/delete/{task_id}")
    public String deleteTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return adminService.delete(token, task_id);
    }
    @GetMapping("/users")
    public List<UserResponse> allUsers(@RequestHeader("Authorization") String token){
        return adminService.showAllUsers(token);
    }
    @PutMapping("/{user_id}/ban")
    public String banUser(@RequestHeader("Authorization") String token, @PathVariable Long user_id){
        return adminService.banUser(token, user_id);
    }
    @PutMapping("/{user_id}/unban")
    public String unbanUser(@RequestHeader("Authorization") String token, @PathVariable Long user_id){
        return adminService.unbanUser(token, user_id);
    }
}
