package com.example.quizley.service;

import com.example.quizley.domain.Origin;
import com.example.quizley.domain.Status;
import com.example.quizley.dto.notification.NotificationResponseDto;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.notification.Notification;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.CommentRepository;
import com.example.quizley.repository.NotificationRepository;
import com.example.quizley.repository.UsersRepository;
import com.example.quizley.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsersRepository usersRepository;
    private final CommentRepository commentRepository;

    // 모든 알림 공통 생성
    public void sendNotification(Long userId, String type, String message) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }


    // 내가 받은 알림 조회
    public List<Notification> getMyNotifications(Long userId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 스케줄러: 아침 리마인더 (오전 9시)
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void morningReminder() {

        List<Users> users = usersRepository.findAll();

        for (Users u : users) {
            sendNotification(u.getUserId(), "REMINDER_MORNING",
                    "좋은 아침입니다! 오늘의 퀴즈 풀고 하루를 시작해보세요!");

        }
    }

    // 스케줄러: 저녁 리마인더 (저녁 7시)
    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void eveningReminder() {

        LocalDate today = LocalDate.now();
        List<Users> users = usersRepository.findAll();

        for (Users u : users) {

            boolean completed =
                    commentRepository.existsByUser_UserIdAndQuiz_OriginAndQuiz_PublishedDateAndStatus(
                            u.getUserId(),
                            Origin.SYSTEM, // SYSTEM 퀴즈
                            today, // 오늘 날짜
                            Status.DONE // DONE 상태
                    );

            if (!completed) {
                sendNotification(u.getUserId(), "REMINDER_EVENING",
                        "오늘의 퀴즈 아직 안 풀었어요! 연속 학습이 끊기지 않도록 해요 :)");

            }
        }
    }


    // 스토리 알림: 1년 전 오늘
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void lastYearStory() {

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        // 1년 전 오늘의 질문에 대한 사용자 답변만 조회
        List<Comment> oldAnswers = commentRepository.findSystemAnswersByQuizDate(oneYearAgo);

        for (Comment c : oldAnswers) {
            Long userId = c.getUser().getUserId();

            sendNotification(userId, "STORY",
                    "1년 전 오늘 답변한 질문이 있어요! 다시 돌아보고 답해볼까요?");

        }
    }

    public List<NotificationResponseDto> getMyNotificationDtos(Long userId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(n -> NotificationResponseDto.builder()
                        .notificationId(n.getNotificationId())
                        .message(n.getMessage())
                        .type(n.getType())
                        .isRead(n.getIsRead())
                        .createdAt(TimeFormatUtil.formatTimeAgo(n.getCreatedAt()))
                        .build())
                .toList();
    }
}
