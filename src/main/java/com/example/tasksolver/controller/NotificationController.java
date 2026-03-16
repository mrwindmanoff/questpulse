package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.NotificationService;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

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

    @GetMapping
    public String notifications(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("notifications", notificationService.getAllNotifications(currentUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        
        return "notifications";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        notificationService.markAllAsRead(currentUser);
        return "redirect:/notifications";
    }
}