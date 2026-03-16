package com.example.tasksolver.repository;

import com.example.tasksolver.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @Query("SELECT a FROM Activity a ORDER BY a.timestamp DESC LIMIT 10")
    List<Activity> findTop10Latest();
}