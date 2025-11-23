package com.example.quizley.dto.community;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayQuizDto {
    private Long quizId;
    private String content;
    private String category;
    private LocalDate publishedDate;
    private Boolean isLiked;
}
