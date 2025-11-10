package com.example.quizley.entity.balance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "balance_answer")
public class BalanceAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;  // PK

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;  // 퀴즈 ID

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 유저 ID

    @Column(nullable = false)
    private String side; // 선택한 답변 (A/B)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 응답 날짜
}
