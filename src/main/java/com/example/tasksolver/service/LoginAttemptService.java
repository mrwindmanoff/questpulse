package com.example.tasksolver.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    public static final int MAX_REGISTRATION_ATTEMPT = 1; // Максимум 1 аккаунт с IP
    public static final int MAX_FAILED_ATTEMPT = 5; // Максимум 5 неудачных попыток
    
    private LoadingCache<String, Integer> registrationAttemptsCache;
    private LoadingCache<String, Integer> failedAttemptsCache;

    public LoginAttemptService() {
        super();
        
        // Кэш для отслеживания количества зарегистрированных аккаунтов с IP (хранится год)
        registrationAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(365, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
        
        // Кэш для отслеживания неудачных попыток (блокировка на 24 часа)
        failedAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void registrationSucceeded(String key) {
        int attempts;
        try {
            attempts = registrationAttemptsCache.get(key);
        } catch (final ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        registrationAttemptsCache.put(key, attempts);
    }

    public void registrationFailed(String key) {
        int attempts;
        try {
            attempts = failedAttemptsCache.get(key);
        } catch (final ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        failedAttemptsCache.put(key, attempts);
    }

    public boolean isIpBlocked(String ip) {
        try {
            int failedAttempts = failedAttemptsCache.get(ip);
            return failedAttempts >= MAX_FAILED_ATTEMPT;
        } catch (final ExecutionException e) {
            return false;
        }
    }

    public boolean canRegisterFromIp(String ip) {
        try {
            int registeredAccounts = registrationAttemptsCache.get(ip);
            return registeredAccounts < MAX_REGISTRATION_ATTEMPT;
        } catch (final ExecutionException e) {
            return true;
        }
    }

    public int getRegisteredAccountsFromIp(String ip) {
        try {
            return registrationAttemptsCache.get(ip);
        } catch (final ExecutionException e) {
            return 0;
        }
    }

    public String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}