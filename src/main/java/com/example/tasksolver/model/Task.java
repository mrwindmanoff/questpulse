package com.example.tasksolver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String correctAnswer;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int rewardXp;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // автоматически устанавливаем награду по сложности
        this.rewardXp = difficulty.getReward();
    }

    // Конструкторы
    public Task() {}

    public Task(String title, String description, String correctAnswer, Difficulty difficulty, User author) {
        this.title = title;
        this.description = description;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
        this.author = author;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public int getRewardXp() { return rewardXp; }
    public void setRewardXp(int rewardXp) { this.rewardXp = rewardXp; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}