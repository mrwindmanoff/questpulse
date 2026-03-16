package com.example.tasksolver.config;

import com.example.tasksolver.security.BanFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private BanFilter banFilter;

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
            .addFilterBefore(banFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // Публичные страницы
                .requestMatchers(
                    "/", 
                    "/leaders", 
                    "/register", 
                    "/login", 
                    "/css/**", 
                    "/js/**",
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
                
                // H2 консоль (только для разработки)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                
                // API для счётчика онлайн (публичный)
                .requestMatchers("/api/online-count").permitAll()
                
                // Страницы, требующие авторизации
                .requestMatchers(
                    "/profile", 
                    "/profile/**",
                    "/notifications",
                    "/notifications/**",
                    "/tasks/create", 
                    "/tasks/*/solve",
                    "/tasks/*/report", 
                    "/tasks/*/delete",
                    "/tasks/*/praise"
                ).authenticated()
                
                // Админка (только для администраторов)
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