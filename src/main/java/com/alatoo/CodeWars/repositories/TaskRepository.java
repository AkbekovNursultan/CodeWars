package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
