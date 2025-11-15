package com.example.quizley.dto.community;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailDto {
    private Long quizId;
    private String content;
    private String nickname; // 닉네임 또는 익명
    private Long likeCount;
    private Long commentCount;
    private String createdAt;
    private Boolean isLiked;
    private Origin origin;
    private Boolean canLike; // 좋아요 가능 여부 (SYSTEM이면 false)
    private Boolean canComment; // 댓글 작성 가능 여부 (SYSTEM이면 false)
}
