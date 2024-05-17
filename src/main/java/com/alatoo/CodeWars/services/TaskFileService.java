package com.alatoo.CodeWars.services;

import org.springframework.web.multipart.MultipartFile;

public interface TaskFileService {
    String addTaskFile(String token, Long task_id, MultipartFile file);
    byte[] downloadFile(String fileName);

    String getFileName(Long taskId, Long fileId);
}
