package com.example.quizley.dto.profile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyCommentDto {
    private Long commentId;
    private Long quizId;
    private String content;
    private String createdAt;
    private int likeCount;
    private String author; // 게시글 작성자
}