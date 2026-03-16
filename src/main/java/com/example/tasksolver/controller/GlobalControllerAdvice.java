package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.NotificationService;
import com.example.tasksolver.service.OnlineUserService;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private OnlineUserService onlineUserService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;

    @ModelAttribute("onlineCount")
    public int getOnlineCount() {
        return onlineUserService.getOnlineCount();
    }
    
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            return userService.findByUsername(username);
        }
        return null;
    }
    
    @ModelAttribute("unreadNotificationsCount")
    public int getUnreadNotificationsCount() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return notificationService.getUnreadCount(currentUser);
        }
        return 0;
    }
}