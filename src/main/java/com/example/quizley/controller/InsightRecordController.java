package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.dto.insight.InsightRecordResponseDto;
import com.example.quizley.dto.insight.SameQuestionAnswerRequestDto;
import com.example.quizley.dto.insight.SameQuestionAnswerResponseDto;

import com.example.quizley.service.CalendarService;
import com.example.quizley.service.InsightRecordService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/insight/record")
@RequiredArgsConstructor
public class InsightRecordController {

    private final InsightRecordService insightRecordService;
    private final CalendarService calendarService;

    // 특정 날짜의 인사이트 조회
    @GetMapping("/{date}")
    public ResponseEntity<?> getInsightByDate(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        Long userId = me.getId();
        List<InsightRecordResponseDto> insights =
                insightRecordService.getInsightByDate(userId, date);

        return ResponseEntity.ok(insights);
    }

    // 인사이트 삭제
    @DeleteMapping("/{date}")
    public ResponseEntity<?> deleteInsight(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        Long userId = me.getId();

        // 1) 해당 날짜 인사이트 soft delete
        insightRecordService.deleteInsight(date, userId);

        // 2) 삭제 이후, 같은 날짜 다시 조회 → "삭제된 인사이트입니다" 포함된 리스트 반환
        List<InsightRecordResponseDto> insights =
                insightRecordService.getInsightByDate(userId, date);

        return ResponseEntity.ok(insights);
    }


    // "같은 질문 다시 답해보기" 리스트 조회
    @GetMapping("/{quizId}/answers")
    public ResponseEntity<?> getSameQuestionAnswers(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        Long userId = me.getId();
        List<SameQuestionAnswerResponseDto> answers =
                insightRecordService.getSameQuestionAnswers(userId, quizId);

        return ResponseEntity.ok(answers);
    }

    // 같은 질문 새 답변 등록
    @PostMapping("/{quizId}/answers")
    public ResponseEntity<?> addSameQuestionAnswer(
            @PathVariable Long quizId,
            @RequestBody SameQuestionAnswerRequestDto request,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        Long userId = me.getId();
        SameQuestionAnswerResponseDto saved =
                insightRecordService.addSameQuestionAnswer(userId, quizId, request);

        return ResponseEntity.ok(saved);
    }
}
