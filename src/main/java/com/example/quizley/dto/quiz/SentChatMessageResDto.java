package com.example.quizley.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// AI 답변 전달
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentChatMessageResDto {
    private Long chatId;

    // 방금 보낸 사용자 메시지
    private ChatMessageResDto userMessage;

    // 방금 받은 AI 메시지
    private ChatMessageResDto aiMessage;

    // "이전 AI + 현재 USER" 기준 한 줄 요약
    private String summary;
}
