package com.example.quizley.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class ChatCommentOpenFormDto {
    private boolean comment_anonymous;
    private boolean writer_anonymous;
}
