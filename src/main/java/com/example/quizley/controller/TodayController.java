package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.domain.Category;
import com.example.quizley.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;


// 오늘의 퀴즈
@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final QuizService quizService;

    // 오늘의 퀴즈 조회
    @GetMapping(value = "")
    public ResponseEntity<?> findTodayQuiz(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam Category category
    ) throws Exception {
        // 권한이 없을 때
        if (me == null) return ResponseEntity.status(401).build();

        // 카테고리가 비어있을 때
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }

        // 오늘 날짜 조회
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DayOfWeek dow = today.getDayOfWeek();

        // 주말 여부 확인
        boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

        // 평일이라면
        if (!weekend) {
            return ResponseEntity.ok(quizService.getWeekdayQuiz(today, String.valueOf(category), me.getId()));
        }

        // TODO : 주말일 때 데이터 반환
        return ResponseEntity.ok(quizService.getWeekdayQuiz(today, String.valueOf(category), me.getId()));
    }
}
