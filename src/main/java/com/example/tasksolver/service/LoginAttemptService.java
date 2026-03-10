package com.example.tasksolver.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    public static final int MAX_REGISTRATION_ATTEMPT = 1; // Максимум 1 аккаунт с IP
    public static final int MAX_FAILED_ATTEMPT = 5; // Максимум 5 неудачных попыток
    private LoadingCache<String, Integer> registrationAttemptsCache;
    private LoadingCache<String, Integer> failedAttemptsCache;

    @Autowired
    private HttpServletRequest request;

    public LoginAttemptService() {
        super();
        
        // Кэш для отслеживания количества зарегистрированных аккаунтов с IP (хранится вечно)
        registrationAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(365, TimeUnit.DAYS) // храним год
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

    public boolean isIpBlocked() {
        String ip = getClientIP();
        
        // Проверяем, не заблокирован ли IP из-за слишком частых попыток
        try {
            int failedAttempts = failedAttemptsCache.get(ip);
            if (failedAttempts >= MAX_FAILED_ATTEMPT) {
                return true;
            }
        } catch (final ExecutionException e) {
            // ignore
        }
        
        return false;
    }

    public boolean canRegisterFromIp() {
        String ip = getClientIP();
        try {
            int registeredAccounts = registrationAttemptsCache.get(ip);
            return registeredAccounts < MAX_REGISTRATION_ATTEMPT;
        } catch (final ExecutionException e) {
            return true;
        }
    }

    public int getRegisteredAccountsFromIp() {
        String ip = getClientIP();
        try {
            return registrationAttemptsCache.get(ip);
        } catch (final ExecutionException e) {
            return 0;
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}