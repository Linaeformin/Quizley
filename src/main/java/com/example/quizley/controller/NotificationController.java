package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(notificationService.getMyNotificationDtos(me.getId()));
    }
}

