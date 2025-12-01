package com.example.quizley.dto.community;

import com.example.quizley.domain.Category;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.util.TimeFormatUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizListDto {
    private Long quizId;
    private Long userId;
    private String content;
    private String category;
    private Long likeCount;
    private Long commentCount;
    private LocalDate publishedDate;
    private String nickname;
    private Boolean isLiked;
    private String createdAt;
    private Boolean isMine;

    //생성자
    public QuizListDto(Long quizId, String content, Category category, String nickname,
                       Long likeCount, Long commentCount, LocalDateTime createdAt,
                       LocalDate publishedDate, Boolean isLiked, Boolean isMine, Long userId) {
        this.quizId = quizId;
        this.userId = userId;
        this.content = content;
        this.category = category.name();
        this.nickname = (nickname != null) ? nickname : "익명";
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = TimeFormatUtil.formatTimeAgo(createdAt);
        this.publishedDate = publishedDate;
        this.isLiked = isLiked;
        this.isMine = false;
    }
}
