package com.example.quizley.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter @Setter
@Builder
public class WeekendQuizCreatedFormDto {
    private String content;         // 퀴즈 내용
    private LocalDate publishedDate;

    private String optionALabel;
    private String optionBLabel;

}
