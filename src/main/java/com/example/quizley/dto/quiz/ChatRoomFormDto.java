package com.example.quizley.dto.quiz;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


// 홈-AI 채팅방 생성 시 프론트에서 받아오는 데이터
@Getter @Setter
public class ChatRoomFormDto {
    @NotBlank
    private Long quizId;

    @Nullable
    private String content;
}
