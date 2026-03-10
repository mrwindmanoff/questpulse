package com.example.tasksolver.controller;

import com.example.tasksolver.model.User;
import com.example.tasksolver.service.EmailService;
import com.example.tasksolver.service.LoginAttemptService;
import com.example.tasksolver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "logout";
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
        
        String clientIp = loginAttemptService.getClientIP();
        
        if (loginAttemptService.isIpBlocked(clientIp)) {
            model.addAttribute("error", "Слишком много попыток регистрации с вашего IP. Попробуйте через 24 часа.");
            return "register";
        }
        
        if (!loginAttemptService.canRegisterFromIp(clientIp)) {
            model.addAttribute("error", "С вашего IP уже зарегистрирован аккаунт. Максимум 1 аккаунт с одного IP.");
            return "register";
        }
        
        if (!password.equals(confirmPassword)) {
            loginAttemptService.registrationFailed(clientIp);
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }

        boolean registered = userService.registerUser(username, password, email);
        if (!registered) {
            loginAttemptService.registrationFailed(clientIp);
            model.addAttribute("error", "Имя пользователя или email уже заняты");
            return "register";
        }

        loginAttemptService.registrationSucceeded(clientIp);

        User user = userService.findByUsername(username);
        String code = generateVerificationCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        userService.save(user);

        // Попытка отправить письмо (не критично, если не получится)
        try {
            String message = "Ваш код подтверждения для QuestPulse: " + code + "\nКод действителен 24 часа.";
            emailService.sendSimpleEmail(user.getEmail(), "Подтверждение email на QuestPulse", message);
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
        }

        return "redirect:/verify-email?username=" + username;
    }

    @GetMapping("/verify-email")
    public String verifyEmailForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String username,
                              @RequestParam String code,
                              Model model) {
        User user = userService.findByUsername(username);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден");
            return "verify-email";
        }

        if (user.isEmailVerified()) {
            model.addAttribute("error", "Email уже подтверждён");
            return "verify-email";
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            model.addAttribute("error", "Неверный код подтверждения");
            model.addAttribute("username", username);
            return "verify-email";
        }

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Код подтверждения истёк. Зарегистрируйтесь заново.");
            return "verify-email";
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userService.save(user);

        return "redirect:/login?verified";
    }

    // ========== ВОССТАНОВЛЕНИЕ ПАРОЛЯ (РАБОТАЕТ БЕЗ ПОЧТЫ) ==========

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

        // Генерируем токен
        String token = java.util.UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userService.save(user);

        // Формируем ссылку для сброса
        String resetLink = baseUrl + "/reset-password?token=" + token;

        // Вместо отправки email показываем ссылку прямо на странице
        model.addAttribute("success", "Ссылка для сброса пароля сгенерирована. Нажмите на неё или скопируйте в браузер:");
        model.addAttribute("resetLink", resetLink);

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