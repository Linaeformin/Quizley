package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizDetailResponse {
    private QuizDetailDto quiz;
    private List<CommentDto> comments;
}
