package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {


    @Query("SELECT DISTINCT t " +
            "FROM Task t " +
            "LEFT JOIN FETCH t.answeredUsers au " +
            "ORDER BY SIZE(t.answeredUsers) DESC")
    List<Task> findAllTasksWithAnsweredUsersCount();
}
