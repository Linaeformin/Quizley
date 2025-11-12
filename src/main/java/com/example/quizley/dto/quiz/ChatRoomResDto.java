package com.example.quizley.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


// 채팅방 접속 시 프론트에게 전달하는 데이터
@Getter @Setter
@Builder
public class ChatRoomResDto {
    private Long chatId;
    private String category;
    private String date;
    private List<ChatMessageResDto> dto;

    // date, category 정제
}
