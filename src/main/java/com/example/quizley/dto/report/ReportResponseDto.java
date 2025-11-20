package com.example.quizley.dto.report;

import lombok.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReportResponseDto {
    private int streakDays; // 연속 답변일 수
    private double topPercent; // 상위 몇 퍼센트인지
    private String dominantCategory; // 가장 많이 참여한 카테고리명
    private Map<String, Integer> scores; // 카테고리별 참여 횟수
    private String feedback; // AI 피드백 (AI는 사용 안함)
}
