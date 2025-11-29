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
    private Boolean isLiked; // 사용자가 해당 게시글에 좋아요를 눌렀는지 여부
    private Origin origin;
    private Boolean canLike; // 좋아요 기능 활성화 여부, SYSTEM 퀴즈는 좋아요 불가 (SYSTEM이면 false)
    private Boolean canComment; // 댓글 작성 기능 활성화 여부, SYSTEM 퀴즈는 작성 불가 (SYSTEM이면 false)
    private Boolean isMine;
}
