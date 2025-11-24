package com.example.quizley.dto.insight;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InsightCombinedResponseDto {
    private List<InsightRecordResponseDto> insights;            // 날짜별 인사이트 기록
    private List<SameQuestionAnswerResponseDto> pastAnswers;    // 같은 질문 과거 답변
}

