package com.example.quizley.dto.community;

import com.example.quizley.domain.Category;
import com.example.quizley.util.TimeFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotQuizDto {
    private Long quizId;
    private String content;
    private String category;
    private Long likeCount;
    private Long commentCount;
    private String nickname;
    private String createdAt;
    private Boolean isLiked;
    private Boolean isMine;

    //생성자
    public HotQuizDto(Long quizId, String content, Category category, String nickname,
                      long likeCount, long commentCount, LocalDateTime createdAt, boolean isLiked, boolean isMine) {
        this.quizId = quizId;
        this.content = content;
        this.category = category.name();
        this.nickname = (nickname != null) ? nickname : "익명";
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = TimeFormatUtil.formatTimeAgo(createdAt);
        this.isLiked = isLiked;
        this.isMine = false;
    }
}
