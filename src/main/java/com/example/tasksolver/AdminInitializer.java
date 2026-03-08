package com.example.tasksolver;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
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

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL:macareokrugin@yandex.ru}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        // Проверяем, что пароль задан
        if (adminPassword == null || adminPassword.isBlank()) {
            System.err.println("❌ ADMIN_PASSWORD не задан! Администратор не может быть создан.");
            return;
        }

        // Создаём администратора MrWindMan, если его нет
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setAdmin(true);
            admin.setTotalXp(999999);
            userRepository.save(admin);
            System.out.println("✅ Администратор " + adminUsername + " создан.");
        } else {
            // Если уже существует, убедимся что он админ
            userRepository.findByUsername(adminUsername).ifPresent(user -> {
                if (!user.isAdmin()) {
                    user.setAdmin(true);
                    userRepository.save(user);
                    System.out.println("✅ Пользователь " + adminUsername + " назначен администратором.");
                }
            });
        }
    }
}