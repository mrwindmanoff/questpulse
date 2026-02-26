package com.example.tasksolver.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private int totalXp = 0;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> createdTasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolvedTask> solvedTasks = new ArrayList<>();

    // Конструкторы
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public List<Task> getCreatedTasks() { return createdTasks; }
    public List<SolvedTask> getSolvedTasks() { return solvedTasks; }

    // Метод для вычисления звания на основе XP
    public String getRank() {
        if (totalXp < 100) return "Новичок";
        else if (totalXp < 500) return "Ученик";
        else if (totalXp < 1000) return "Эксперт";
        else if (totalXp < 2000) return "Мастер";
        else return "Легенда";
    }
}