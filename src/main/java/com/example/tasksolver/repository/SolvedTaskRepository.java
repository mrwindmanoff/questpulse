package com.example.tasksolver.repository;

import com.example.tasksolver.model.SolvedTask;
import com.example.tasksolver.model.User;
import com.example.tasksolver.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SolvedTaskRepository extends JpaRepository<SolvedTask, Long> {
    Optional<SolvedTask> findByUserAndTask(User user, Task task);
    boolean existsByUserAndTask(User user, Task task);
    void deleteByTask(Task task);
    
    @Query("SELECT COUNT(s) FROM SolvedTask s WHERE s.user = :user")
    int countByUser(@Param("user") User user);
}