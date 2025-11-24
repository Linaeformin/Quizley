package com.example.quizley.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsightRecordResponseDto {
    private Long quizId;        // 퀴즈 ID
    private String category;    // 카테고리 (예: 미스터리)
    private LocalDate date;     // 공개일
    private String question;    // 퀴즈 내용
    private String summary;     // AI 요약
    private String feedback;    // AI 피드백
}