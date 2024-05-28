package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.ReviewDto;
import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.services.TaskFileService;
import com.alatoo.CodeWars.services.TaskService;
import com.alatoo.CodeWars.dto.task.SearchTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskFileService taskFileService;
    @GetMapping("/all")
    public List<TaskResponse> showAllTasks(@RequestHeader("Authorization") String token,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.showAllTasks(token, locale);
    }
    @GetMapping("/search")
    public List<TaskResponse> search(@RequestHeader("Authorization") String token, @RequestBody SearchTaskRequest searchRequest,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.search(token, searchRequest, locale);
    }
    @GetMapping("/{task_id}")
    public TaskDetailsResponse showTaskDetails(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.showById(token, task_id, locale);
    }
    @PostMapping("/{task_id}/favorite")
    public String markFavorite(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.markFavorite(token, task_id, locale);
    }
    @GetMapping("/favorites")
    public List<TaskResponse> showFavorites(@RequestHeader("Authorization") String token,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.showFavorites(token, locale);
    }
    @GetMapping("/download/{task_id}/{file_id}")
    public ResponseEntity<ByteArrayResource> downloadTaskFile(@PathVariable Long task_id, @PathVariable Long file_id){
        String fileName = taskFileService.getFileName(task_id, file_id);
        byte[] data = taskFileService.downloadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename\"" + fileName +"\"")
                .body(resource);
    }
    @PostMapping("/{task_id}/answer")
    public String attemptAnswer(@RequestHeader("Authorization") String token, @RequestParam String answer, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.attempt(token, task_id, answer, locale);
    }
    @PostMapping("/{task_id}/rate")
    public String rateTask(@RequestHeader("Authorization") String token, @PathVariable Long task_id, @RequestBody ReviewDto reviewDto,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.addReview(token, task_id, reviewDto, locale);
    }
    @GetMapping("/{task_id}/reviews")
    public List<ReviewDto> reviews(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.showAllReviews(token, task_id, locale);
    }
    @GetMapping("/{task_id}/get_hint")
    public String getHint(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.getHint(token, task_id, locale);
    }
    @GetMapping("/{task_id}/received_hints")
    public List<String> receivedHints(@RequestHeader("Authorization") String token, @PathVariable Long task_id,@RequestHeader(name = "Accept-Language", required = false) Locale locale){
        return taskService.getHints(token, task_id, locale);
    }
}
