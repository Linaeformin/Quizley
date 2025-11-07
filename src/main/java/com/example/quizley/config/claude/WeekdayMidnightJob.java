package com.example.quizley.config.claude;

import com.example.quizley.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;


// AI 평일 질문 생성
@Component
@RequiredArgsConstructor
public class WeekdayMidnightJob {

    private final PromptLoader promptLoader;
    private final ClaudeGateway claude;
    private final QuizService quizService;

    // 서울 시각 기준 평일 매일 00:00 실행
    @Scheduled(cron = "0 0 0 ? * MON-FRI", zone = "${app.timezone:Asia/Seoul}")
    public void run() {
        try {
            // 1) 프롬프트 로드
            String prompt = promptLoader.load("weekday");

            // 2) API 호출
            Map<String,String> map = claude.generateMapFromPrompt(prompt);

            // 3) 저장
            int saved = quizService.saveSystemWeekdayBulk(map);

        } catch (Exception e) {

        }
    }
}
