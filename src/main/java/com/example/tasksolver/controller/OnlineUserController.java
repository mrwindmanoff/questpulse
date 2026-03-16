package com.example.tasksolver.controller;

import com.example.tasksolver.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OnlineUserController {

    @Autowired
    private OnlineUserService onlineUserService;

    @GetMapping("/api/online-count")
    @ResponseBody
    public int getOnlineCount() {
        return onlineUserService.getOnlineCount();
    }
}