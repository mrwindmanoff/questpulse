package com.example.tasksolver.controller;

import com.example.tasksolver.dto.TaskForm;
import com.example.tasksolver.model.User;
import com.example.tasksolver.service.TaskService;
import com.example.tasksolver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userService.findByUsername(username);
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("taskForm", new TaskForm());
        return "task-create";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("taskForm") TaskForm form,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "task-create";
        }
        User author = getCurrentUser();
        if (author == null) {
            return "redirect:/login";
        }
        // Проверка на бан
        if (author.isBanned()) {
            redirectAttributes.addFlashAttribute("error", "Ваш аккаунт забанен. Вы не можете создавать задачи.");
            return "redirect:/";
        }
        taskService.createTask(form, author);
        redirectAttributes.addFlashAttribute("message", "Задача успешно создана!");
        return "redirect:/";
    }

    @GetMapping("/{id}/solve")
    public String showSolveForm(@PathVariable Long id, Model model) {
        var task = taskService.findTaskById(id);
        if (task == null) {
            return "redirect:/?error=notfound";
        }
        model.addAttribute("task", task);
        return "task-solve";
    }

    @PostMapping("/{id}/solve")
    public String solveTask(@PathVariable Long id,
                            @RequestParam("answer") String answer,
                            RedirectAttributes redirectAttributes) {
        User solver = getCurrentUser();
        if (solver == null) {
            return "redirect:/login";
        }
        // Проверка на бан
        if (solver.isBanned()) {
            redirectAttributes.addFlashAttribute("error", "Ваш аккаунт забанен. Вы не можете решать задачи.");
            return "redirect:/";
        }

        var result = taskService.solveTask(id, answer, solver);
        switch (result) {
            case SUCCESS:
                redirectAttributes.addFlashAttribute("message", "Верно! +XP");
                break;
            case CANNOT_SOLVE_OWN_TASK:
                redirectAttributes.addFlashAttribute("error", "Вы не можете решать свою собственную задачу");
                break;
            case ALREADY_SOLVED:
                redirectAttributes.addFlashAttribute("error", "Вы уже решали эту задачу");
                break;
            case WRONG_ANSWER:
                redirectAttributes.addFlashAttribute("error", "Неверный ответ");
                break;
            case TASK_NOT_FOUND:
                redirectAttributes.addFlashAttribute("error", "Задача не найдена");
                break;
        }
        return "redirect:/tasks/" + id + "/solve";
    }

    @PostMapping("/{id}/report")
    public String reportTask(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User reporter = getCurrentUser();
        if (reporter == null) {
            return "redirect:/login";
        }
        // Проверка на бан
        if (reporter.isBanned()) {
            redirectAttributes.addFlashAttribute("error", "Ваш аккаунт забанен.");
            return "redirect:/";
        }

        var result = taskService.reportTask(id, reporter);
        switch (result) {
            case SUCCESS:
                redirectAttributes.addFlashAttribute("message", "Жалоба отправлена!");
                break;
            case CANNOT_REPORT_OWN_TASK:
                redirectAttributes.addFlashAttribute("error", "Вы не можете жаловаться на свою задачу");
                break;
            case ALREADY_REPORTED:
                redirectAttributes.addFlashAttribute("error", "Вы уже жаловались на эту задачу");
                break;
            case TASK_NOT_FOUND:
                redirectAttributes.addFlashAttribute("error", "Задача не найдена");
                break;
        }
        return "redirect:/tasks/" + id + "/solve";
    }

    @PostMapping("/{id}/praise")
    public String praiseTask(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        // Проверка на бан
        if (user.isBanned()) {
            redirectAttributes.addFlashAttribute("error", "Ваш аккаунт забанен.");
            return "redirect:/";
        }

        var result = taskService.praiseTask(id, user);
        switch (result) {
            case SUCCESS:
                redirectAttributes.addFlashAttribute("message", "Задача получила похвалу!");
                break;
            case ALREADY_PRAISED:
                redirectAttributes.addFlashAttribute("error", "Вы уже хвалили эту задачу");
                break;
            case TASK_NOT_FOUND:
                redirectAttributes.addFlashAttribute("error", "Задача не найдена");
                break;
        }
        return "redirect:/tasks/" + id + "/solve";
    }

    @GetMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        // Проверка на бан (хотя удаление и так может быть запрещено)
        if (currentUser.isBanned()) {
            redirectAttributes.addFlashAttribute("error", "Ваш аккаунт забанен.");
            return "redirect:/";
        }
        boolean deleted = taskService.deleteTask(id, currentUser);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Задача удалена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить задачу (может, вы не автор?)");
        }
        return "redirect:/";
    }
}