package com.example.quizley.entity.balance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "quiz_balance")
public class QuizBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;  // PK

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;  // 퀴즈 ID

    @Column(nullable = false)
    private String side; // 선택지 구분 (A 또는 B)

    @Column(length = 20)
    private String label; // 선택지 텍스트 (짜장, 짬뽕, 피자, 파스타 등)

    @Column(name = "img_url", length = 200)
    private String imgUrl; // 선택지 이미지 URL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;
}
