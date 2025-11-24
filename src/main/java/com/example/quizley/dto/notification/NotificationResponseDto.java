package com.example.quizley.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseDto {

    private Long notificationId;
    private String message;
    private String type;       // 예: COMMENT, REMINDER, STORY 등
    private Boolean isRead;
    private String createdAt;
}
