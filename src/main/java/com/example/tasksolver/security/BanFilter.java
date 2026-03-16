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
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Пропускаем, если пользователь не аутентифицирован
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = auth.getName();
            
            // Проверяем, забанен ли пользователь
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.isBanned()) {
                    logger.info("Banned user {} tried to access the site", username);
                    
                    // Очищаем контекст безопасности
                    SecurityContextHolder.clearContext();
                    
                    // Инвалидируем сессию
                    if (request.getSession(false) != null) {
                        request.getSession(false).invalidate();
                    }
                    
                    // Перенаправляем на страницу входа с сообщением о бане
                    try {
                        response.sendRedirect("/login?banned");
                    } catch (IOException e) {
                        logger.error("Error redirecting banned user", e);
                    }
                }
            });
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Error in BanFilter", e);
            filterChain.doFilter(request, response);
        }
    }
}