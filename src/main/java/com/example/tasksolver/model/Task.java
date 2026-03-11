package com.example.tasksolver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    // Количество жалоб
    private int reportCount = 0;
    
    // Количество похвал (лайков) — новое поле
    private int praiseCount = 0;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Report> reports = new HashSet<>();
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Praise> praises = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public int getPraiseCount() { return praiseCount; }
    public void setPraiseCount(int praiseCount) { this.praiseCount = praiseCount; }

    public Set<Report> getReports() { return reports; }
    public void setReports(Set<Report> reports) { this.reports = reports; }
    
    public Set<Praise> getPraises() { return praises; }
    public void setPraises(Set<Praise> praises) { this.praises = praises; }
}