package com.example.quizley.entity.quiz;

import com.example.quizley.entity.quiz.QuizLikeId;
import com.example.quizley.entity.users.Users;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_like")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(QuizLikeId.class)
public class QuizLike {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
