package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.NewDifficultyRequest;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.services.AdminService;
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
    @PostMapping("/add/difficulty")
    public String addDifficulty(@RequestHeader("Authorization") String token, @RequestBody NewDifficultyRequest request){
        return adminService.addDifficulty(token, request);
    }
    @PostMapping("/add/task")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request){
        return taskService.addTask(token, request);
    }
    @PostMapping("/add/{task_id}")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return taskService.addTaskFile(token, task_id, file);
    }
    @GetMapping("/show/{task_id}")
    public TaskDetailsResponse showTaskDetails(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskService.showById(token, task_id);
    }
    @GetMapping("/show/task_offers")
    public List<TaskResponse> showAllOffers(@RequestHeader("Authorization") String token){
        return adminService.showAllOffers(token);
    }
    @DeleteMapping("/delete/{task_id}")
    public String deleteTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return adminService.delete(token, task_id);
    }
}
