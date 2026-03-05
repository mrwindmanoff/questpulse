package com.example.tasksolver.repository;

import com.example.tasksolver.model.Report;
import com.example.tasksolver.model.Task;
import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserAndTask(User user, Task task);
    boolean existsByUserAndTask(User user, Task task);
    void deleteByTask(Task task);
}