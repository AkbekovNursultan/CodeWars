package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.image.ImageResponse;
import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
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
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final TaskService taskService;
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
    @GetMapping("/tasks")
    public List<TaskResponse> showAllTasks(@RequestHeader("Authorization") String token){
        return taskService.showAllTasks(token);
    }
    @GetMapping("/task/{task_id}")
    public TaskDetailsResponse showTaskDetails(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskService.showById(token, task_id);
    }
    @GetMapping("/task/{task_id}/download")
    public ResponseEntity<ByteArrayResource> downloadTaskFile(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        List <String> fileNames = taskService.getFileNames(token, task_id);
        byte[] data = taskService.downloadFile(fileNames);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filenames\"" + fileNames +"\"")
                .body(resource);
    }
    @PostMapping("/task/add")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request){
        return userService.addTask(token, request);
    }
    @PostMapping("/task/{task_id}/file")
    public String addTaskFile(@RequestHeader("Authorizaition") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return userService.addTaskFile(token, task_id, file);
    }
}
