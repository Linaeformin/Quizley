package com.example.quizley.config.claude;

import com.example.quizley.service.AdminQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// AI 평일 질문 생성
@Component
@RequiredArgsConstructor
public class WeekdayMidnightJob {

    private final AdminQuizService adminQuizService;

    // 서울 시각 기준 평일 매일 00:00 실행
    @Scheduled(cron = "0 0 0 ? * MON-FRI", zone = "${app.timezone:Asia/Seoul}")
    public void run() {
        try {
            int saved = adminQuizService.generateWeekdayQuizFromAi();
            // 필요하면 saved 로깅
        } catch (Exception e) {
            // TODO: 로그 남기기
        }
    }
}
