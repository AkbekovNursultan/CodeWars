package com.alatoo.CodeWars.repositories;

import com.alatoo.CodeWars.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User> findByEmail(String email);

    Optional<User> findByRecoveryCode(String code);
}
