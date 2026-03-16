package com.example.tasksolver.security;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BanFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BanFilter.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Пропускаем статические ресурсы
        String path = request.getRequestURI();
        if (path.startsWith("/css/") || path.startsWith("/js/") || 
            path.startsWith("/h2-console") || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Пропускаем страницу логина и выхода
        if (path.equals("/login") || path.equals("/logout") || path.equals("/logout-success")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Если пользователь не аутентифицирован - пропускаем
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = auth.getName();
            logger.debug("Checking ban status for user: {}", username);
            
            // Проверяем бан
            boolean isBanned = false;
            try {
                User user = userRepository.findByUsername(username).orElse(null);
                isBanned = user != null && user.isBanned();
            } catch (Exception e) {
                logger.error("Database error while checking ban for user: {}", username, e);
                // При ошибке БД пропускаем запрос
                filterChain.doFilter(request, response);
                return;
            }
            
            if (isBanned) {
                logger.info("Banned user {} tried to access the site", username);
                
                // Очищаем контекст безопасности
                SecurityContextHolder.clearContext();
                
                // Инвалидируем сессию
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                
                // Перенаправляем на страницу входа с сообщением о бане
                // И ВАЖНО: не вызываем дальше filterChain
                response.sendRedirect("/login?banned");
                return;
            }
            
            // Если не забанен - продолжаем цепочку
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in BanFilter", e);
            // В случае любой ошибки пропускаем запрос, чтобы сайт работал
            filterChain.doFilter(request, response);
        }
    }
}