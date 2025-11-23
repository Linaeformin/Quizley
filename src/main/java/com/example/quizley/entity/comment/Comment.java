package com.example.quizley.entity.comment;

import com.example.quizley.domain.CommentAnonymous;
import com.example.quizley.domain.Status;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.users.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


// 응답 및 댓글 엔티티
@Entity
@Table(name = "comment")
@Getter @Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // PROGRESS/DONE

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_anonymous", nullable = false)
    private CommentAnonymous commentAnonymous; // OPEN/CLOSE

    @Column(name = "writer_anonymous", nullable = false)
    private boolean writerAnonymous;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private java.time.LocalDateTime modifiedAt;

    private java.time.LocalDateTime deletedAt;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @PrePersist
    void prePersist() {
        var now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (modifiedAt == null) modifiedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
