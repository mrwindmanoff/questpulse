package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/make/{username}")
    public String makeAdmin(@PathVariable String username) {
        User currentUser = getCurrentUser();
        // Только существующий администратор может назначать других
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/?error=notAuthorized";
        }

        User user = userService.findByUsername(username);
        if (user != null) {
            user.setAdmin(true);
            userService.save(user);
        }
        return "redirect:/?message=adminAssigned";
    }
}