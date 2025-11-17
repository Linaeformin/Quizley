package com.example.quizley.dto.community;

import com.example.quizley.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateDto {
    @NotBlank(message = "질문 내용을 입력해주세요.")
    private String content;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Category category;
    private Boolean isAnonymous; // 익명 여부 (null이면 false)
}