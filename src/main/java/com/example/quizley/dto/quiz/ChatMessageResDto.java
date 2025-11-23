package com.example.quizley.dto.quiz;

import com.example.quizley.domain.MessageOrigin;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


// 메시지 목록 DTO
@Getter @Setter
@Builder
public class ChatMessageResDto {
    private MessageOrigin origin;
    private String message;
    private String date;
}
