package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.EmailService;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }
        boolean registered = userService.registerUser(username, password, email);
        if (!registered) {
            model.addAttribute("error", "Имя пользователя или email уже заняты");
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Пользователь с таким email не найден");
            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userService.save(user);

        String resetLink = baseUrl + "/reset-password?token=" + token;
        String message = "To reset your password, click the link: " + resetLink;

        try {
            emailService.sendSimpleEmail(user.getEmail(), "Восстановление пароля на QuestPulse", message);
            model.addAttribute("success", "Инструкция по сбросу пароля отправлена на ваш email");
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при отправке письма: " + e.getMessage());
            e.printStackTrace(); // для логов
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        User user = userService.findByResetPasswordToken(token);
        if (user == null || user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Ссылка недействительна или истекла");
            return "reset-password-error";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("token", token);
            return "reset-password";
        }

        User user = userService.findByResetPasswordToken(token);
        if (user == null || user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Ссылка недействительна или истекла");
            return "reset-password-error";
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userService.save(user);

        return "redirect:/login?resetSuccess";
    }
}