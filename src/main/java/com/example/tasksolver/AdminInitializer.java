package com.example.tasksolver;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:MrWindMan}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:Ma04082012}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL:macareokrugin@yandex.ru}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        // Создаём администратора MrWindMan, если его нет
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setAdmin(true);
            admin.setTotalXp(999999); // чтобы был на вершине таблицы лидеров
            userRepository.save(admin);
            System.out.println("✅ Администратор " + adminUsername + " создан.");
        } else {
            // Если уже существует, убедимся что он админ
            userRepository.findByUsername(adminUsername).ifPresent(user -> {
                if (!user.isAdmin()) {
                    user.setAdmin(true);
                    userRepository.save(user);
                    System.out.println("✅ Пользователь " + adminUsername + " назначен администратором.");
=======
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("${app.admins}")
    private String adminNames;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminNames == null || adminNames.isBlank()) {
            return;
        }

        List<String> adminUsernameList = Arrays.stream(adminNames.split(","))
                .map(String::trim)
                .toList();

        for (String username : adminUsernameList) {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (!user.isAdmin()) {
                    user.setAdmin(true);
                    userRepository.save(user);
                    System.out.println("Пользователь " + username + " назначен администратором.");
>>>>>>> 6f15291b8250e93787fb26f60411efa1b1f12c85
                }
            });
        }
    }
}