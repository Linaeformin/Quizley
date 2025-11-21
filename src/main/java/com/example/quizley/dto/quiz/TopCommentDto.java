package com.example.quizley.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// [홈] TopComment 리스트
@Getter @Setter
@NoArgsConstructor
public class TopCommentDto {
    private Long commentId;
    private String comment;
}
