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

    // IP-based limits (мягкие)
    public static final int MAX_REGISTRATION_PER_IP = 5; // 5 аккаунтов с одного IP (для офиса/класса)
    public static final int MAX_FAILED_ATTEMPT = 10; // максимум неудачных попыток
    
    // Fingerprint-based limits (жёсткие)
    public static final int MAX_REGISTRATION_PER_FINGERPRINT = 1; // 1 аккаунт на браузер
    
    private LoadingCache<String, Integer> ipRegistrationCache;
    private LoadingCache<String, Integer> fingerprintRegistrationCache;
    private LoadingCache<String, Integer> failedAttemptsCache;

    @Autowired
    private FingerprintService fingerprintService;

    public LoginAttemptService() {
        super();
        
        ipRegistrationCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
        
        fingerprintRegistrationCache = CacheBuilder.newBuilder()
                .expireAfterWrite(365, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
        
        failedAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void registrationSucceeded(String ip, String fingerprint) {
        // IP-based
        int ipAttempts = getIpAttempts(ip);
        ipRegistrationCache.put(ip, ipAttempts + 1);
        
        // Fingerprint-based
        int fpAttempts = getFingerprintAttempts(fingerprint);
        fingerprintRegistrationCache.put(fingerprint, fpAttempts + 1);
    }

    public void registrationFailed(String ip) {
        int attempts = getFailedAttempts(ip);
        failedAttemptsCache.put(ip, attempts + 1);
    }

    public boolean isIpBlocked(String ip) {
        return getFailedAttempts(ip) >= MAX_FAILED_ATTEMPT;
    }

    public boolean canRegisterFromIp(String ip) {
        return getIpAttempts(ip) < MAX_REGISTRATION_PER_IP;
    }

    public boolean canRegisterFromFingerprint(String fingerprint) {
        return getFingerprintAttempts(fingerprint) < MAX_REGISTRATION_PER_FINGERPRINT;
    }

    public int getIpAttempts(String ip) {
        try {
            return ipRegistrationCache.get(ip);
        } catch (final ExecutionException e) {
            return 0;
        }
    }

    public int getFingerprintAttempts(String fingerprint) {
        try {
            return fingerprintRegistrationCache.get(fingerprint);
        } catch (final ExecutionException e) {
            return 0;
        }
    }

    public int getFailedAttempts(String ip) {
        try {
            return failedAttemptsCache.get(ip);
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
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public void resetIpAttempts(String ip) {
        ipRegistrationCache.put(ip, 0);
    }

    public void resetFailedAttempts(String ip) {
        failedAttemptsCache.put(ip, 0);
    }
}