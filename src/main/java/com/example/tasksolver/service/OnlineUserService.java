package com.example.tasksolver.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {
    
    private final Map<String, Long> onlineUsers = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 5 * 60 * 1000; // 5 минут
    
    public void userActivity(String username) {
        onlineUsers.put(username, System.currentTimeMillis());
    }
    
    public void userDisconnected(String username) {
        onlineUsers.remove(username);
    }
    
    public int getOnlineCount() {
        long now = System.currentTimeMillis();
        onlineUsers.entrySet().removeIf(entry -> 
            now - entry.getValue() > TIMEOUT
        );
        return onlineUsers.size();
    }
}