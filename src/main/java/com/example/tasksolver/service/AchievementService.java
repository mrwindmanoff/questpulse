package com.example.tasksolver.service;

import com.example.tasksolver.model.Achievement;
import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.AchievementRepository;
import com.example.tasksolver.repository.SolvedTaskRepository;
import com.example.tasksolver.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private SolvedTaskRepository solvedTaskRepository;

    @Transactional
    public void checkAndAwardAchievements(User user) {
        // Первая созданная задача
        if (!achievementRepository.existsByUserAndType(user, "FIRST_TASK")) {
            int createdCount = taskRepository.countByAuthor(user);
            if (createdCount >= 1) {
                achievementRepository.save(new Achievement(
                    user, "FIRST_TASK", "Создатель", "Создал первую задачу"
                ));
            }
        }
        
        // 10 созданных задач
        if (!achievementRepository.existsByUserAndType(user, "TEN_TASKS")) {
            int createdCount = taskRepository.countByAuthor(user);
            if (createdCount >= 10) {
                achievementRepository.save(new Achievement(
                    user, "TEN_TASKS", "Плодовитый автор", "Создал 10 задач"
                ));
            }
        }
        
        // Первое решение
        if (!achievementRepository.existsByUserAndType(user, "FIRST_SOLVE")) {
            int solvedCount = solvedTaskRepository.countByUser(user);
            if (solvedCount >= 1) {
                achievementRepository.save(new Achievement(
                    user, "FIRST_SOLVE", "Решатель", "Решил первую задачу"
                ));
            }
        }
        
        // 10 решений
        if (!achievementRepository.existsByUserAndType(user, "TEN_SOLVES")) {
            int solvedCount = solvedTaskRepository.countByUser(user);
            if (solvedCount >= 10) {
                achievementRepository.save(new Achievement(
                    user, "TEN_SOLVES", "Опытный решатель", "Решил 10 задач"
                ));
            }
        }
    }
}