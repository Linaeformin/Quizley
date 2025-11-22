package com.example.quizley.controller;

import com.example.quizley.common.ApiSuccess;
import com.example.quizley.dto.quiz.WeekendQuizCreatedFormDto;
import com.example.quizley.service.AdminQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // 주말 질문 생성
    @PostMapping(
            value = "/balance",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createBalanceGame(
            @RequestPart("request") WeekendQuizCreatedFormDto request,   // JSON
            @RequestPart("optionAImage") MultipartFile optionAImage,    // 파일
            @RequestPart("optionBImage") MultipartFile optionBImage     // 파일
    ) throws Exception {

        adminQuizService.createBalanceGame(request, optionAImage, optionBImage);
        return ResponseEntity.ok(
                new ApiSuccess(200, "성공적으로 처리되었습니다.")
        );
    }
}

