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
    private String content;
    private String category;
    private Long likeCount;
    private Long commentCount;
    private LocalDate publishedDate;
    private String nickname;
    private Boolean isLiked;
    private String createdAt;

    //생성자
    public QuizListDto(Long quizId, String content, Category category, String nickname,
                       long likeCount, long commentCount, LocalDateTime createdAt,
                       LocalDate publishedDate, boolean isLiked) {
        this.quizId = quizId;
        this.content = content;
        this.category = category.name();
        this.nickname = (nickname != null) ? nickname : "익명";
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = TimeFormatUtil.formatTimeAgo(createdAt);
        this.publishedDate = publishedDate;
        this.isLiked = isLiked;
    }
}
