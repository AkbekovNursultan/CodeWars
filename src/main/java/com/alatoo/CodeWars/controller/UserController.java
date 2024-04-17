package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.user.ImageResponse;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.dto.user.UserInfoResponse;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.ImageService;
import com.alatoo.CodeWars.services.TaskService;
import com.alatoo.CodeWars.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ImageService imageService;
    private final TaskService taskService;
    @GetMapping("/profile/{user_id}")
    public UserInfoResponse userInfo(@RequestHeader("Authorization") String token, @PathVariable Long user_id){
        return userService.showUserInfo(token, user_id);
    }
    @GetMapping("/profile/{user_id}/image")
    public ImageResponse showImage(@RequestHeader("Authorization") String token, @PathVariable Long user_id){
        return imageService.showByUser(token, user_id);
    }
    @PostMapping("/profile/image/add")
    public String upload(@RequestHeader("Authorization") String token, @RequestParam(value = "file") MultipartFile file){
        return imageService.upload(token, file);
    }
    @DeleteMapping("/profile/image/delete")
    public String deleteImage(@RequestHeader("Authorization") String token){
        return imageService.deleteFile(token);
    }
    @PostMapping("/task/add")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request){
        return userService.addTask(token, request);
    }
    @PostMapping("/task/{task_id}/file")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return userService.addTaskFile(token, task_id, file);
    }
    @DeleteMapping("/task/{task_id}/delete_files")
    public String deleteFiles(@PathVariable Long task_id){
        return taskService.deleteTaskFiles(task_id);
    }
}
