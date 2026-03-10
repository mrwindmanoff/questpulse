package com.example.tasksolver.config;

import com.example.tasksolver.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                // Публичные страницы
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
                    "/user/**",
                    "/logout-success"
                ).permitAll()
                
                // Подтверждение email
                .requestMatchers(
                    "/verify-email",
                    "/verify-email/**",
                    "/resend-code",
                    "/resend-code/**"
                ).permitAll()
                
                // H2 консоль
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                
                // Страницы для авторизованных
                .requestMatchers(
                    "/profile", 
                    "/profile/**",
                    "/tasks/create", 
                    "/tasks/*/solve",
                    "/tasks/*/report", 
                    "/tasks/*/delete"
                ).authenticated()
                
                // Админка
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Всё остальное требует авторизации
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout-success")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}