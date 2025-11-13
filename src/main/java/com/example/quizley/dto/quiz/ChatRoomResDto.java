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
    private List<ChatMessageResDto> messages;

    // 페이징 메타
    private int totalPages;   // 총 페이지 수
    private int maxPages;     // 마지막 페이지 번호
    private int currentPage;  // 현재 페이지(0부터 시작)
    private boolean hasPrev;  // 이전 페이지 존재 여부
    private boolean hasNext;  // 다음 페이지 존재 여부
}
