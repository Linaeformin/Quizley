package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekendQuizDetailResponse {
    private Long quizId;
    private String content;
    private LocalDate publishedDate;
    private WeekendQuizVoteResultDto voteResult;
    private List<CommentDto> comments;
}
