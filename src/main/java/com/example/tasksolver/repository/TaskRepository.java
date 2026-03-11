package com.example.tasksolver.repository;

import com.example.tasksolver.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Сортировка сначала по похвалам (убывание), потом по дате создания (убывание)
    @Query("SELECT t FROM Task t ORDER BY t.praiseCount DESC, t.createdAt DESC")
    List<Task> findAllByOrderByPraiseCountDescCreatedAtDesc();
}