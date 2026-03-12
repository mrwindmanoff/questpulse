package com.example.tasksolver.controller;

import com.example.tasksolver.model.Task;
import com.example.tasksolver.model.User;
import com.example.tasksolver.service.TaskService;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userService.findByUsername(auth.getName());
    }

    @GetMapping("/users")
    public String usersList(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        model.addAttribute("users", userService.findAll());
        model.addAttribute("bannedUsers", userService.getBannedUsers());
        model.addAttribute("currentUser", currentUser);
        return "admin-users";
    }

    @PostMapping("/ban/{username}")
    public String banUser(@PathVariable String username,
                          @RequestParam String reason) {
        User admin = getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        User user = userService.findByUsername(username);
        if (user == null || user.isAdmin()) {
            return "redirect:/admin/users?error=banFailed";
        }

        List<Task> tasksToDelete = new ArrayList<>(user.getCreatedTasks());
        for (Task task : tasksToDelete) {
            taskService.deleteTask(task.getId(), admin);
        }

        boolean banned = userService.banUser(username, reason, admin);
        if (banned) {
            return "redirect:/admin/users?banned=" + username;
        } else {
            return "redirect:/admin/users?error=banFailed";
        }
    }

    @PostMapping("/unban/{username}")
    public String unbanUser(@PathVariable String username) {
        User admin = getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        boolean unbanned = userService.unbanUser(username, admin);
        if (unbanned) {
            return "redirect:/admin/users?unbanned=" + username;
        } else {
            return "redirect:/admin/users?error=unbanFailed";
        }
    }

    @PostMapping("/make-admin/{username}")
    public String makeAdmin(@PathVariable String username,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        User admin = getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        User user = userService.findByUsername(username);
        if (user != null && !user.isAdmin()) {
            user.setAdmin(true);
            userService.save(user);
            
            // Если мы назначаем админом не себя, разлогиниваем пользователя
            if (!admin.getUsername().equals(username)) {
                // Разлогиниваем назначенного админа (если он сейчас онлайн)
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName().equals(username)) {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                }
                return "redirect:/admin/users?madeAdmin=" + username + "&logoutRequired=true";
            }
            
            return "redirect:/admin/users?madeAdmin=" + username;
        }
        return "redirect:/admin/users?error=makeAdminFailed";
    }

    @PostMapping("/remove-admin/{username}")
    public String removeAdmin(@PathVariable String username,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        User admin = getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        if (admin.getUsername().equals(username)) {
            return "redirect:/admin/users?error=cannotRemoveSelf";
        }

        User user = userService.findByUsername(username);
        if (user != null && user.isAdmin()) {
            user.setAdmin(false);
            userService.save(user);
            
            // Разлогиниваем пользователя, у которого сняли админку (если он онлайн)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName().equals(username)) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            
            return "redirect:/admin/users?removedAdmin=" + username + "&logoutRequired=true";
        }
        return "redirect:/admin/users?error=removeAdminFailed";
    }
}