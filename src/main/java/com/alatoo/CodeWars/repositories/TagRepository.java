package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
