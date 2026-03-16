package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.AchievementRepository;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementRepository achievementRepository;

    @GetMapping("/user/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/?error=userNotFound";
        }
        model.addAttribute("profileUser", user);
        model.addAttribute("userAchievements", achievementRepository.findByUserOrderByEarnedAtDesc(user));
        return "public-profile";
    }
}