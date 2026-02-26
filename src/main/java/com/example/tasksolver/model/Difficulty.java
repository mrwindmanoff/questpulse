package com.example.tasksolver.model;

public enum Difficulty {
    EASY(10),
    MEDIUM(25),
    HARD(50);

    private final int reward;

    Difficulty(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }
}