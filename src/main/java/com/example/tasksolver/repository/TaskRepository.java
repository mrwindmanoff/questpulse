package com.example.tasksolver.repository;

import com.example.tasksolver.model.Task;
import com.example.tasksolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query("SELECT t FROM Task t ORDER BY t.praiseCount DESC, t.createdAt DESC")
    List<Task> findAllByOrderByPraiseCountDescCreatedAtDesc();
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.author = :author")
    int countByAuthor(@Param("author") User author);
}