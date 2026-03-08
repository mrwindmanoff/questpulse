package com.example.tasksolver.config;

import com.example.tasksolver.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Публичные страницы (доступны всем без авторизации)
                .requestMatchers(
                    "/", 
                    "/leaders", 
                    "/register", 
                    "/login", 
                    "/css/**", 
                    "/forgot-password", 
                    "/forgot-password/**",
                    "/reset-password", 
                    "/reset-password/**",
                    "/reset-password-error",
                    "/user/**"
                ).permitAll()
                
                // Страницы подтверждения email (доступны без авторизации)
                .requestMatchers(
                    "/verify-email",
                    "/verify-email/**",
                    "/resend-code",
                    "/resend-code/**"
                ).permitAll()
                
                // H2 консоль (только для разработки)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                
                // Страницы, требующие авторизации
                .requestMatchers(
                    "/profile",
                    "/profile/**",
                    "/tasks/create",
                    "/tasks/*/solve",
                    "/tasks/*/report",
                    "/tasks/*/delete"
                ).authenticated()
                
                // Админка (только для администраторов)
                .requestMatchers(
                    "/admin/**"
                ).hasAuthority("ROLE_ADMIN")  // используем hasAuthority, так как у нас нет ролей, а есть флаг admin
                
                // Всё остальное разрешено
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // Отключаем CSRF для H2 консоли
            .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
            // Разрешаем фреймы для H2 консоли
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}