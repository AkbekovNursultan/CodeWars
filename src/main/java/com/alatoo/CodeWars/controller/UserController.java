package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.image.ImageResponse;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.repositories.UserRepository;
import com.alatoo.CodeWars.services.AuthService;
import com.alatoo.CodeWars.services.ImageService;
import org.springframework.core.io.ByteArrayResource;
import com.alatoo.CodeWars.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageService imageService;
    @GetMapping("/image")
    public ImageResponse showImage(@RequestHeader("Authorization") String token){
        return imageService.showByUser(token);
    }
    @PostMapping("/image/add")
    public String upload(@RequestHeader("Authorization") String token, @RequestParam(value = "file") MultipartFile file){
        imageService.upload(token, file);
        return "Done";
    }
    @DeleteMapping("/image/delete")
    public String deleteImage(@RequestHeader("Authorization") String token){
        return imageService.deleteFile(token);
    }
    @PostMapping("/add/task")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request){
        return userService.addTask(token, request);
    }
    @PostMapping("/add/{task_id}")
    public String addTaskFile(@RequestHeader("Authorizaition") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return userService.addTaskFile(token, task_id, file);
    }
}