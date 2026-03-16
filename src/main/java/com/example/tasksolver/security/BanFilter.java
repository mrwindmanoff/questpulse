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
import java.util.Arrays;
import java.util.List;

@Component
public class BanFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BanFilter.class);

    @Autowired
    private UserRepository userRepository;

    // Список путей, которые не требуют проверки бана
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/css/", "/js/", "/h2-console", "/favicon.ico",
        "/login", "/logout", "/logout-success", "/register",
        "/forgot-password", "/reset-password", "/api/online-count"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 1. Проверяем публичные пути
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. Если пользователь не аутентифицирован - пропускаем
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = auth.getName();
            logger.debug("Checking ban status for user: {}", username);
            
            // 3. Проверяем бан
            User user = null;
            try {
                user = userRepository.findByUsername(username).orElse(null);
            } catch (Exception e) {
                logger.error("Database error while checking ban for user: {}", username, e);
                filterChain.doFilter(request, response);
                return;
            }
            
            // 4. Если пользователь забанен
            if (user != null && user.isBanned()) {
                logger.info("Banned user {} tried to access: {}", username, path);
                
                // Очищаем контекст безопасности
                SecurityContextHolder.clearContext();
                
                // Инвалидируем сессию
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                
                // Перенаправляем на страницу входа с сообщением о бане
                response.sendRedirect("/login?banned");
                return; // ВАЖНО: не продолжаем цепочку
            }
            
            // 5. Если не забанен - продолжаем
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Unexpected error in BanFilter for path: {}", path, e);
            // В случае любой ошибки пропускаем запрос, чтобы сайт работал
            filterChain.doFilter(request, response);
        }
    }
}