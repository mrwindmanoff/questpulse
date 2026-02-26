package com.example.tasksolver.service;

import com.example.tasksolver.dto.TaskForm;
import com.example.tasksolver.model.*;
import com.example.tasksolver.repository.SolvedTaskRepository;
import com.example.tasksolver.repository.TaskRepository;
import com.example.tasksolver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolvedTaskRepository solvedTaskRepository;

    public List<Task> findAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Transactional
    public void createTask(TaskForm form, User author) {
        Task task = new Task();
        task.setTitle(form.getTitle());
        task.setDescription(form.getDescription());
        task.setCorrectAnswer(form.getCorrectAnswer());
        task.setDifficulty(form.getDifficulty());
        task.setAuthor(author);
        taskRepository.save(task);
    }

    @Transactional
    public SolveResult solveTask(Long taskId, String answer, User solver) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return SolveResult.TASK_NOT_FOUND;
        }

        // Запрещаем автору решать свою задачу
        if (task.getAuthor().getId().equals(solver.getId())) {
            return SolveResult.CANNOT_SOLVE_OWN_TASK;
        }

        // Проверяем, решал ли уже эту задачу
        if (solvedTaskRepository.existsByUserAndTask(solver, task)) {
            return SolveResult.ALREADY_SOLVED;
        }

        // Сравниваем ответ (без учета регистра, обрезаем пробелы)
        if (!task.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim())) {
            return SolveResult.WRONG_ANSWER;
        }

        // Всё верно – начисляем XP
        int reward = task.getRewardXp();

        // Решателю
        solver.setTotalXp(solver.getTotalXp() + reward);
        userRepository.save(solver);

        // Автору
        User author = task.getAuthor();
        author.setTotalXp(author.getTotalXp() + reward);
        userRepository.save(author);

        // Запись о решении
        SolvedTask solved = new SolvedTask(solver, task);
        solvedTaskRepository.save(solved);

        return SolveResult.SUCCESS;
    }

    @Transactional
    public boolean deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;
        
        // Проверяем, что текущий пользователь - автор задачи
        if (!task.getAuthor().getId().equals(currentUser.getId())) {
            return false;
        }
        
        // Сначала удаляем все связи SolvedTask (иначе ошибка внешнего ключа)
        solvedTaskRepository.deleteByTask(task);
        
        // Теперь удаляем саму задачу
        taskRepository.delete(task);
        return true;
    }

    public enum SolveResult {
        SUCCESS,
        TASK_NOT_FOUND,
        ALREADY_SOLVED,
        WRONG_ANSWER,
        CANNOT_SOLVE_OWN_TASK
    }
}