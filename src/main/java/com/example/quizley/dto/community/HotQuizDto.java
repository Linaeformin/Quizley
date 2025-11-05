package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotQuizDto {
    private Long quizId;
    private String content;
    private String category;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;
}
