package com.example.quizley.dto.community;

import com.example.quizley.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityHomeResponse {
    private LocalDate date;
    private String category; //현재 선택된 카테고리
    private List<HotQuizDto> hotQuiz; //카테고리당 인기글 3개 반환
    private List<QuizListDto> quizzes; //해당 카테고리의 오늘의 질문, 사용자 퀴즈
    private TodayQuizDto todayQuiz; //오늘의 퀴즈
}
