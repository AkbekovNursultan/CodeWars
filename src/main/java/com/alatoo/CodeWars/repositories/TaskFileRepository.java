package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.TaskFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskFileRepository extends JpaRepository<TaskFile, Long> {
}
