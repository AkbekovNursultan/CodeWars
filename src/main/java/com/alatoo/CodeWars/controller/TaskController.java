package com.alatoo.CodeWars.controller;

import com.alatoo.CodeWars.dto.task.TaskDetailsResponse;
import com.alatoo.CodeWars.dto.task.TaskResponse;
import com.alatoo.CodeWars.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    @GetMapping("/all")
    public List<TaskResponse> showAllTasks(@RequestHeader("Authorization") String token){
        return taskService.showAllTasks(token);
    }
    @GetMapping("/{task_id}")
    public TaskDetailsResponse showTaskDetails(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskService.showById(token, task_id);
    }
    @GetMapping("/{task_id}/download/{file_id}")
    public ResponseEntity<ByteArrayResource> downloadTaskFile(@PathVariable Long task_id, @PathVariable Long file_id){
        String fileName = taskService.getFileName(task_id, file_id);
        byte[] data = taskService.downloadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename\"" + fileName +"\"")
                .body(resource);
    }
    @PostMapping("/{task_id}/answer")
    public String attemptAnswer(@RequestHeader("Authorization") String token, @RequestParam String answer, @PathVariable Long task_id){
        return taskService.attempt(token, task_id, answer);
    }
    @GetMapping("/{task_id}/get_hint")
    public String getHint(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskService.getHint(token, task_id);
    }
    @GetMapping("/{task_id}/received_hints")
    public List<String> receivedHints(@RequestHeader("Authorization") String token, @PathVariable Long task_id){
        return taskService.getHints(token, task_id);
    }
}
