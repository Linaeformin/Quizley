package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeekendCommunityHomeResponse {
    private LocalDate date;
    private String category;  // 추가
    private WeekendQuizDto weekendQuiz;
    private List<HotQuizDto> hotQuiz;  // 추가 - 인기 게시글 3개
    private List<QuizListDto> quizzes;  // 추가 - 유저 생성 퀴즈
}