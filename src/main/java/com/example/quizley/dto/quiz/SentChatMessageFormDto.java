package com.example.quizley.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;


// 프론트에서 메시지 데이터 받아오기
@Getter
@NoArgsConstructor
public class SentChatMessageFormDto {
    private String message;
}
