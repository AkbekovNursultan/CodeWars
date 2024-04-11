package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Image;
import com.alatoo.CodeWars.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByUser(User user);
}
