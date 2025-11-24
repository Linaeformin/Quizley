package com.example.quizley.repository;

import com.example.quizley.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.quizley.entity.users.Users;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
}
