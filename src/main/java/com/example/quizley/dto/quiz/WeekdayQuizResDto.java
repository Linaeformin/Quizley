package com.example.quizley.dto.quiz;

import com.example.quizley.domain.Category;
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
    private LocalDate published_date;
    private boolean completed;

    // 엔티티 -> DTO 변환
    public static WeekdayQuizResDto of(Quiz q, boolean completed) {
        return WeekdayQuizResDto.builder()
                .quizId(q.getQuizId())
                .content(q.getContent())
                .category(q.getCategory())
                .published_date(q.getPublishedDate())
                .completed(completed)
                .build();
    }
}


