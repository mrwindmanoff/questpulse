package com.example.tasksolver.service;

import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registerUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return false;
        }
        User user = new User(username, passwordEncoder.encode(password), email);
        user.setEmailVerified(false);
        userRepository.save(user);
        return true;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token).orElse(null);
    }

    public User findByVerificationCode(String code) {
        return userRepository.findByVerificationCode(code).orElse(null);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean banUser(String username, String reason, User admin) {
        if (!admin.isAdmin()) return false;

        User user = findByUsername(username);
        if (user == null || user.isAdmin()) return false;

        user.setBanned(true);
        user.setBanReason(reason);
        user.setBannedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    public boolean unbanUser(String username, User admin) {
        if (!admin.isAdmin()) return false;

        User user = findByUsername(username);
        if (user == null) return false;

        user.setBanned(false);
        user.setBanReason(null);
        user.setBannedAt(null);
        userRepository.save(user);
        return true;
    }

    public List<User> getBannedUsers() {
        return userRepository.findByBannedTrue();
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public boolean isUserBanned(String username) {
        User user = findByUsername(username);
        return user != null && user.isBanned();
    }
}