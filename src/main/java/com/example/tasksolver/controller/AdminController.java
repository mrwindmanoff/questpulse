package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

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

        model.addAttribute("users", userService.findAll()); // нужен метод в сервисе
        model.addAttribute("bannedUsers", userService.getBannedUsers());
        return "admin-users";
    }

    @PostMapping("/ban/{username}")
    public String banUser(@PathVariable String username,
                          @RequestParam String reason,
                          Model model) {
        User admin = getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/?error=notAuthorized";
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
}