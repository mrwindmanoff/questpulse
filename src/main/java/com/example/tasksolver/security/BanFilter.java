package com.example.tasksolver.security;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BanFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.isBanned()) {
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();
                    try {
                        response.sendRedirect("/login?banned");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        filterChain.doFilter(request, response);
    }
}