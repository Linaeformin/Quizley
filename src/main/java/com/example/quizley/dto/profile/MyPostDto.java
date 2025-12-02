package com.example.quizley.dto.profile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPostDto {
    private Long quizId;
    private String content;
    private String category;
    private String createdAt;
    private Boolean isAnonymous;
    private int commentCount;
    private int likeCount;
}
