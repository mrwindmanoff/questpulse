package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/leaders")
public class LeaderboardController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String leaderboard(Model model) {
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "totalXp"))
        ).getContent();
        model.addAttribute("users", topUsers);
        return "leaderboard";
    }
}