package com.example.tasksolver.repository;

import com.example.tasksolver.model.Achievement;
import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserOrderByEarnedAtDesc(User user);
    
    boolean existsByUserAndType(User user, String type);
    
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.user = :user")
    int countByUser(@Param("user") User user);
}