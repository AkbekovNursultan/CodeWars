package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Hint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HintRepository extends JpaRepository<Hint, Long> {
}
