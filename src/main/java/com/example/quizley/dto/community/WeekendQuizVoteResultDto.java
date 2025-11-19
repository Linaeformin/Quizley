package com.example.quizley.dto.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekendQuizVoteResultDto {
    private String sideALabel;
    private String sideAImageUrl;
    private Integer sideAPercentage;

    private String sideBLabel;
    private String sideBImageUrl;
    private Integer sideBPercentage;
}
