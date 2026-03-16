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
    private PraiseRepository praiseRepository;
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private NotificationService notificationService;

    public List<Task> findAllTasks() {
        return taskRepository.findAllByOrderByPraiseCountDescCreatedAtDesc();
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
        
        activityRepository.save(new Activity(
            author.getUsername(), 
            "CREATED_TASK", 
            form.getTitle()
        ));
        
        achievementService.checkAndAwardAchievements(author);
    }

    @Transactional
    public SolveResult solveTask(Long taskId, String answer, User solver) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return SolveResult.TASK_NOT_FOUND;
        }

        if (task.getAuthor().getId().equals(solver.getId())) {
            return SolveResult.CANNOT_SOLVE_OWN_TASK;
        }

        if (solvedTaskRepository.existsByUserAndTask(solver, task)) {
            return SolveResult.ALREADY_SOLVED;
        }

        if (!task.getCorrectAnswer().trim().equalsIgnoreCase(answer.trim())) {
            return SolveResult.WRONG_ANSWER;
        }

        int reward = task.getRewardXp();

        solver.setTotalXp(solver.getTotalXp() + reward);
        userRepository.save(solver);

        User author = task.getAuthor();
        author.setTotalXp(author.getTotalXp() + reward);
        userRepository.save(author);

        SolvedTask solved = new SolvedTask(solver, task);
        solvedTaskRepository.save(solved);
        
        activityRepository.save(new Activity(
            solver.getUsername(), 
            "SOLVED_TASK", 
            task.getTitle()
        ));
        
        achievementService.checkAndAwardAchievements(solver);

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
        
        activityRepository.save(new Activity(
            reporter.getUsername(), 
            "REPORTED_TASK", 
            task.getTitle()
        ));

        return ReportResult.SUCCESS;
    }
    
    @Transactional
    public PraiseResult praiseTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return PraiseResult.TASK_NOT_FOUND;
        }

        if (praiseRepository.existsByUserAndTask(user, task)) {
            return PraiseResult.ALREADY_PRAISED;
        }

        Praise praise = new Praise(user, task);
        praiseRepository.save(praise);

        task.setPraiseCount(task.getPraiseCount() + 1);
        taskRepository.save(task);
        
        activityRepository.save(new Activity(
            user.getUsername(), 
            "PRAISED_TASK", 
            task.getTitle()
        ));

        return PraiseResult.SUCCESS;
    }

    @Transactional
    public boolean deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        boolean isAuthor = task.getAuthor().getId().equals(currentUser.getId());
        if (!(currentUser.isAdmin() || isAuthor)) {
            return false;
        }

        if (!isAuthor && currentUser.isAdmin()) {
            User author = task.getAuthor();
            int penalty = task.getReportCount();
            if (penalty > 0) {
                author.setTotalXp(Math.max(0, author.getTotalXp() - penalty));
                userRepository.save(author);
            }

            for (Report report : task.getReports()) {
                User reporter = report.getUser();
                reporter.setTotalXp(reporter.getTotalXp() + 1);
                userRepository.save(reporter);
                notificationService.createNotification(reporter, "REPORT_REWARD", 
                    "Ваша жалоба на задачу \"" + task.getTitle() + "\" была рассмотрена. +1 XP");
            }
        }

        solvedTaskRepository.deleteByTask(task);
        reportRepository.deleteByTask(task);
        praiseRepository.deleteByTask(task);
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
    
    public enum PraiseResult {
        SUCCESS,
        TASK_NOT_FOUND,
        ALREADY_PRAISED
    }
}