package com.example.tasksolver.repository;

import com.example.tasksolver.model.TaskLike;
import com.example.tasksolver.model.Task;
import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskLikeRepository extends JpaRepository<TaskLike, Long> {
    Optional<TaskLike> findByUserAndTask(User user, Task task);
    boolean existsByUserAndTask(User user, Task task);
    void deleteByTask(Task task);
    int countByTask(Task task);
}