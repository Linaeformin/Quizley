package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeekendQuizDto {
    private Long quizId;
    private String content;
    private LocalDate publishedDate;
    private WeekendQuizVoteResultDto voteResult;
}