package com.example.quizley.controller;

import com.example.quizley.service.AdminQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


// 로컬 환경에서 질문 생성 테스트
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/quiz")
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    // 질문 생성
    @PostMapping("/generate")
    public Map<String,Object> generate() {

        // AdminQuizService 안에서
        // 1) QUIZ 프롬프트 로드
        // 2) ClaudeClient로 AI 호출
        // 3) JSON → Map 파싱
        // 4) QuizService.saveSystemWeekdayBulk(...) 호출
        int saved = adminQuizService.generateWeekdayQuizFromAi();

        return Map.of("saved", saved);
    }
}

