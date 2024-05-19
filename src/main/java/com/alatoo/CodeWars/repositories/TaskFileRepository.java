package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Task;
import com.alatoo.CodeWars.entities.TaskFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskFileRepository extends JpaRepository<TaskFile, Long> {

    List<TaskFile> findByTask(Task task);
}
