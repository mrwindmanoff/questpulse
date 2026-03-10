package com.example.tasksolver.service;

import com.example.tasksolver.dto.TaskForm;
import com.example.tasksolver.model.*;
import com.example.tasksolver.repository.*;
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

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TaskLikeRepository taskLikeRepository;

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
    public ReportResult reportTask(Long taskId, User reporter) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ReportResult.TASK_NOT_FOUND;
        }

        if (task.getAuthor().getId().equals(reporter.getId())) {
            return ReportResult.CANNOT_REPORT_OWN_TASK;
        }

        if (reportRepository.existsByUserAndTask(reporter, task)) {
            return ReportResult.ALREADY_REPORTED;
        }

        Report report = new Report(reporter, task);
        reportRepository.save(report);

        task.setReportCount(task.getReportCount() + 1);
        taskRepository.save(task);

        return ReportResult.SUCCESS;
    }

    // ========== ЛАЙК ЗАДАЧИ (С ЗАПРЕТОМ ДЛЯ АВТОРА) ==========
    @Transactional
    public LikeResult likeTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return LikeResult.TASK_NOT_FOUND;
        }

        // === ЗАПРЕЩАЕМ АВТОРУ ЛАЙКАТЬ СВОЮ ЗАДАЧУ ===
        if (task.getAuthor().getId().equals(user.getId())) {
            return LikeResult.CANNOT_LIKE_OWN_TASK;
        }

        // Проверяем, не лайкал ли уже
        if (taskLikeRepository.existsByUserAndTask(user, task)) {
            return LikeResult.ALREADY_LIKED;
        }

        // Создаём лайк
        TaskLike like = new TaskLike(user, task);
        taskLikeRepository.save(like);

        // Увеличиваем счётчик лайков
        task.setLikeCount(task.getLikeCount() + 1);
        taskRepository.save(task);

        return LikeResult.SUCCESS;
    }

    @Transactional
    public boolean deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        boolean isAuthor = task.getAuthor().getId().equals(currentUser.getId());
        if (!(currentUser.isAdmin() || isAuthor)) {
            return false;
        }

        // Если удаляет администратор (не автор), накладываем штраф на автора
        if (!isAuthor && currentUser.isAdmin()) {
            User author = task.getAuthor();
            int penalty = task.getReportCount();
            if (penalty > 0) {
                author.setTotalXp(Math.max(0, author.getTotalXp() - penalty));
                userRepository.save(author);
            }

            // Начисляем XP всем, кто жаловался (только при удалении админом)
            for (Report report : task.getReports()) {
                User reporter = report.getUser();
                reporter.setTotalXp(reporter.getTotalXp() + 1);
                userRepository.save(reporter);
            }
        }

        // Удаляем все связанные записи
        solvedTaskRepository.deleteByTask(task);
        reportRepository.deleteByTask(task);
        taskLikeRepository.deleteByTask(task);
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

    public enum ReportResult {
        SUCCESS,
        TASK_NOT_FOUND,
        ALREADY_REPORTED,
        CANNOT_REPORT_OWN_TASK
    }

    // ========== ENUM ДЛЯ ЛАЙКОВ ==========
    public enum LikeResult {
        SUCCESS,
        TASK_NOT_FOUND,
        ALREADY_LIKED,
        CANNOT_LIKE_OWN_TASK  // Добавлен запрет
    }
}