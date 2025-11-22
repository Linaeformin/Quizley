package com.example.quizley.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


// 밸런스 게임 제작 요청 DTO
@Getter @Setter
@Builder
public class WeekendQuizCreatedFormDto {
    private String content;
    private LocalDate publishedDate;
    private String optionALabel;
    private String optionBLabel;

}
