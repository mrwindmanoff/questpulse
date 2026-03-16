package com.example.tasksolver.controller;

import com.example.tasksolver.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private OnlineUserService onlineUserService;

    @ModelAttribute("onlineCount")
    public int getOnlineCount() {
        return onlineUserService.getOnlineCount();
    }
}