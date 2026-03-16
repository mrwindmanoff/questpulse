package com.example.tasksolver.service;

import com.example.tasksolver.model.Notification;
import com.example.tasksolver.model.User;
import com.example.tasksolver.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User user, String type, String message) {
        Notification notification = new Notification(user, type, message);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    public int getUnreadCount(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user).size();
    }
}