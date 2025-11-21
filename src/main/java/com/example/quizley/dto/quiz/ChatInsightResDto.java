package com.example.quizley.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;


// [홈] 인사이트 데이터 DTO
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatInsightResDto {
    private Long chatId;
    private Long quizId;
    private String category;
    private String date;
    private String quizName;
    private String summary;
    private String feedback;
    private List<TopCommentDto> topCommentDtoList;
}
