package com.example.tasksolver;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
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
            return; // нет списка админов – ничего не делаем
        }

        // Разбиваем строку с именами через запятую, удаляем пробелы
        List<String> adminUsernameList = Arrays.stream(adminNames.split(","))
                .map(String::trim)
                .toList();

        for (String username : adminUsernameList) {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (!user.isAdmin()) {
                    user.setAdmin(true);
                    userRepository.save(user);
                    System.out.println("Пользователь " + username + " назначен администратором.");
                }
            });
        }
    }
}