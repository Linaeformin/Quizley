package com.example.quizley.entity.users;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.quizley.domain.ContentType;
import com.example.quizley.domain.ReportAction;

@Entity
@Table(name = "report_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId; // PK

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId; // 신고한 유저

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType; // QUESTION, COMMENT

    @Column(name = "content_id", nullable = false)
    private Long contentId; // 신고 대상 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportAction action; // NONE, HIDE_CONTENT, DELETE_CONTENT, SUSPEND_USER

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewer_at")
    private LocalDateTime reviewerAt; // nullable

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (action == null) {
            action = ReportAction.NONE;
        }
    }
}
