package com.example.quizley.dto.quiz;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.QuizType;
import com.example.quizley.entity.quiz.Quiz;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


// 평일 오늘의 질문 반환
@Getter @Setter
@Builder
public class WeekdayQuizResDto {
    private Long quizId;
    private String content;
    private Category category;
    private String publishedDate;
    private boolean completed;
    private QuizType quizType;

    // 엔티티 -> DTO 변환
    public static WeekdayQuizResDto of(Quiz q, boolean completed, String roomDate) {
        return WeekdayQuizResDto.builder()
                .quizId(q.getQuizId())
                .content(q.getContent())
                .category(q.getCategory())
                .publishedDate(roomDate)
                .completed(completed)
                .quizType(QuizType.WEEKDAY)
                .build();
    }
}


