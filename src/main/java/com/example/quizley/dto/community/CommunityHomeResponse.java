package com.example.quizley.dto.community;

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
    private TodayQuizDto todayQuiz;
    private Map<String, List<HotQuizDto>> quizzesByCategory;
    private List<QuizListDto> quizzes;
}
