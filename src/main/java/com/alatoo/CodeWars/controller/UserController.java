package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.user.UserDtoResponse;
import com.alatoo.CodeWars.services.ImageService;
import com.alatoo.CodeWars.services.TaskFileService;
import com.alatoo.CodeWars.services.TaskService;
import com.alatoo.CodeWars.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ImageService imageService;
    private final TaskService taskService;
    private final TaskFileService taskFileService;

    @GetMapping("/profile/{user_id}")
    public UserDtoResponse userInfo(@RequestHeader("Authorization") String token, @PathVariable Long user_id){
        return userService.showUserInfo(token, user_id);
    }
    @GetMapping("/profile/image/{fileName}")
    public ResponseEntity<InputStreamResource> showImage(@RequestHeader("Authorization") String token, @PathVariable String fileName) {
        var s3Object = imageService.getFile(fileName);
        var content = s3Object.getObjectContent();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""+"Profile Photo"+"\"")
                .body(new InputStreamResource(content));
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
        return taskService.addTask(token, request);
    }
    @PostMapping("/task/{task_id}/file")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file){
        return userService.addTaskFile(token, task_id, file);
    }
    @DeleteMapping("/task/{task_id}/delete_files")
    public String deleteFiles(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskFileService.deleteTaskFiles(token, task_id);
    }
}
