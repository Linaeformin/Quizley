package com.example.quizley.dto.quiz;

import com.example.quizley.domain.QuizType;
import com.example.quizley.entity.balance.QuizBalance;
import com.example.quizley.entity.quiz.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class WeekendQuizResDto {

    private Long quizId;
    private String content;
    private String publishedDate;          // "yyyy.MM.dd. (E)" 형식
    private boolean completed;        // 유저 응답 여부
    private QuizType quizType;
    private List<BalanceOptionDto> options;

    @Getter
    @Builder
    public static class BalanceOptionDto {
        private String side;   // "A" or "B"
        private String label;  // 선택지 텍스트
        private String imgUrl; // 이미지 URL
    }

    public static WeekendQuizResDto of(Quiz quiz,
                                       boolean completed,
                                       String roomDate,
                                       List<QuizBalance> balances) {

        List<BalanceOptionDto> optionDtos = balances.stream()
                .map(b -> BalanceOptionDto.builder()
                        .side(b.getSide().name())
                        .label(b.getLabel())
                        .imgUrl(b.getImgUrl())
                        .build())
                .collect(Collectors.toList());

        return WeekendQuizResDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .publishedDate(roomDate)
                .completed(completed)
                .options(optionDtos)
                .quizType(QuizType.WEEKEND)
                .build();
    }
}
