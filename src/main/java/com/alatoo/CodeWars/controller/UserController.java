package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.NewTaskRequest;
import com.alatoo.CodeWars.dto.task.TaskResponse;
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

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ImageService imageService;
    private final TaskService taskService;
    private final TaskFileService taskFileService;

    @GetMapping("/{user_id}/profile")
    public UserDtoResponse userInfo(@RequestHeader("Authorization") String token, @PathVariable Long user_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return userService.showUserInfo(token, user_id, locale);
    }
    @GetMapping("/{user_id}/created_tasks")
    public List<TaskResponse> userTasks(@RequestHeader("Authorization") String token, @PathVariable Long user_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.showUserTasks(token, user_id, locale);
    }
    @GetMapping("/profile/image/{fileName}")
    public ResponseEntity<InputStreamResource> showImage(@RequestHeader("Authorization") String token, @PathVariable String fileName) {
        var s3Object = imageService.getFile(token, fileName);
        var content = s3Object.getObjectContent();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""+"Profile Photo"+"\"")
                .body(new InputStreamResource(content));
    }
    @PostMapping("/profile/image/add")
    public String upload(@RequestHeader("Authorization") String token, @RequestParam(value = "file") MultipartFile file,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return imageService.upload(token, file, locale);
    }
    @DeleteMapping("/profile/image/delete")
    public String deleteImage(@RequestHeader("Authorization") String token,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return imageService.deleteFile(token, locale);
    }

    @PostMapping("/add/task")
    public String addTask(@RequestHeader("Authorization") String token, @RequestBody NewTaskRequest request,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.addTask(token, request, locale);
    }
    @PostMapping("/add/{task_id}")
    public String addTaskFile(@RequestHeader("Authorization") String token,@PathVariable Long task_id , @RequestParam(value = "file") MultipartFile file,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return userService.addTaskFile(token, task_id, file, locale);
    }
    @DeleteMapping("/task/{task_id}/delete_files")
    public String deleteFiles(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskFileService.deleteTaskFiles(token, task_id, locale);
    }
}
