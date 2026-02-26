package com.example.tasksolver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solved_tasks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "task_id"})
})
public class SolvedTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    private LocalDateTime solvedAt;

    @PrePersist
    protected void onCreate() {
        solvedAt = LocalDateTime.now();
    }

    public SolvedTask() {}

    public SolvedTask(User user, Task task) {
        this.user = user;
        this.task = task;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public LocalDateTime getSolvedAt() { return solvedAt; }
}