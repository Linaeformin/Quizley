package com.example.quizley.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SameQuestionAnswerResponseDto {

    private Long answerId;          // balance_answer PK
    private String answer;          // 텍스트 답변
    private LocalDateTime createdAt; // 언제 쓴 답변인지
}
