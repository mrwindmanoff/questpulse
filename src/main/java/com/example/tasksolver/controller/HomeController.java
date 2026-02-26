package com.example.tasksolver.controller;

import com.example.tasksolver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskService.findAllTasks());
        return "index";
    }
}