package com.example.tasksolver.repository;

import com.example.tasksolver.model.Praise;
import com.example.tasksolver.model.Task;
import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PraiseRepository extends JpaRepository<Praise, Long> {
    Optional<Praise> findByUserAndTask(User user, Task task);
    boolean existsByUserAndTask(User user, Task task);
    void deleteByTask(Task task);
}