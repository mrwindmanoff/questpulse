package com.example.tasksolver.controller;

import com.example.tasksolver.model.Activity;
import com.example.tasksolver.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @GetMapping("/fragment/activities")
    public String getActivitiesFragment(Model model) {
        List<Activity> activities = activityRepository.findTop10Latest();
        // Всегда передаём список, даже если он пустой
        model.addAttribute("activities", activities != null ? activities : new ArrayList<>());
        return "fragments/activities :: activitiesList";
    }
}