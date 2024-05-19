package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
