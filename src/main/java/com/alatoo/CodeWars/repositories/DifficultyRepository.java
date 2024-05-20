package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.DifficultyKyu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DifficultyRepository extends JpaRepository<DifficultyKyu, Long> {
    Optional <DifficultyKyu> findByName(String difficulty);
}
