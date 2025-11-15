package com.example.quizley.dto.community;
import com.example.quizley.util.TimeFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long commentId;
    private String content;
    private String nickname; // 작성자 닉네임 또는 익명
    private Integer likeCount;
    private Boolean isLiked;
    private String createdAt;

    // 생성자
    public CommentDto(Long commentId, String content, String nickname,
                      Integer likeCount, Boolean isLiked, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.content = content;
        this.nickname = nickname;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.createdAt = TimeFormatUtil.formatTimeAgo(createdAt);
    }
}
