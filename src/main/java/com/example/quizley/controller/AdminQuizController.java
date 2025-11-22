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
    public ResponseEntity<?> generate() {

        int saved = adminQuizService.generateWeekdayQuizFromAi();

        return ResponseEntity.ok(
                new ApiSuccess(200, "성공적으로 처리되었습니다.")
        );
    }

    // 주말 질문 생성
    @PostMapping(
            value = "/balance",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createBalanceGame(
            @RequestPart("request") WeekendQuizCreatedFormDto request,
            @RequestPart("optionAImage") MultipartFile optionAImage,
            @RequestPart("optionBImage") MultipartFile optionBImage
    ) throws Exception {

        // 밸런스 게임 생성
        adminQuizService.createBalanceGame(request, optionAImage, optionBImage);

        return ResponseEntity.ok(
                new ApiSuccess(200, "성공적으로 처리되었습니다.")
        );
    }
}

