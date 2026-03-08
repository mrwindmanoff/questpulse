package com.example.tasksolver.repository;

import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
    Optional<User> findByVerificationCode(String verificationCode);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByBannedTrue();
}