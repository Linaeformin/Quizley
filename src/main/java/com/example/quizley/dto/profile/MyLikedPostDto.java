package com.example.quizley.dto.profile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyLikedPostDto {
    private Long quizId;
    private String content;
    private String category;
    private String createdAt;
    private String author;
    private int commentCount;
    private int likeCount;
}