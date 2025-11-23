package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSearchResponse {
    private String keyword; // 검색한 키워드
    private Long totalCount; // 전체 검색 결과 개수
    private List<QuizListDto> quizzes; // 검색된 퀴즈 목록
}