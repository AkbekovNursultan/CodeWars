package com.alatoo.CodeWars.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public interface TaskFileService {
    String addTaskFile(String token, Long task_id, MultipartFile file, Locale locale);
    byte[] downloadFile(String fileName);

    String getFileName(Long taskId, Long fileId);
    @Transactional
    String deleteTaskFiles(String token, Long task_id, Locale locale);
}
