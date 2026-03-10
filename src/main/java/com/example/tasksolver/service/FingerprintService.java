package com.example.tasksolver.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;
import java.util.UUID;

@Service
public class FingerprintService {

    private static final String FINGERPRINT_COOKIE = "questpulse_fp";
    private static final int COOKIE_MAX_AGE = 365 * 24 * 60 * 60; // 1 год

    public String getOrCreateFingerprint(HttpServletResponse response) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return UUID.randomUUID().toString();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // Проверяем, есть ли уже кука
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (FINGERPRINT_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // Создаём новый fingerprint
        String fingerprint = UUID.randomUUID().toString();
        
        // Сохраняем в куку
        jakarta.servlet.http.Cookie newCookie = new jakarta.servlet.http.Cookie(FINGERPRINT_COOKIE, fingerprint);
        newCookie.setMaxAge(COOKIE_MAX_AGE);
        newCookie.setPath("/");
        newCookie.setHttpOnly(true);
        response.addCookie(newCookie);
        
        return fingerprint;
    }

    public String getBrowserFingerprint() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // Собираем уникальные характеристики браузера
        String userAgent = request.getHeader("User-Agent");
        String accept = request.getHeader("Accept");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String connection = request.getHeader("Connection");
        
        // Создаём уникальную строку
        String fingerprint = userAgent + "|" + accept + "|" + acceptLanguage + "|" + acceptEncoding + "|" + connection;
        
        // Хешируем (чтобы не хранить личные данные)
        return Base64.getEncoder().encodeToString(fingerprint.getBytes());
    }
}