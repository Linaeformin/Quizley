package com.example.quizley.entity.balance;

import com.example.quizley.domain.BalanceSide;
import com.example.quizley.entity.quiz.Quiz;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BalanceSide side; // 선택지 구분 (A 또는 B)

    @Column(length = 20)
    private String label; // 선택지 텍스트 (짜장, 짬뽕, 피자, 파스타 등)

    @Column(name = "img_url", length = 200)
    private String imgUrl; // 선택지 이미지 URL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    // Quiz 엔티티에서 PK만 뽑아서 세팅하는 버전
    public static QuizBalance of(Quiz quiz, BalanceSide side, String label, String imgUrl) {
        QuizBalance qb = new QuizBalance();
        qb.quizId = quiz.getQuizId();   // 또는 quiz.getId() 네 엔티티에 맞게
        qb.side = side;
        qb.label = label;
        qb.imgUrl = imgUrl;
        return qb;
    }

    // 만약 quizId만 알고 있을 때 쓰고 싶으면 이런 오버로드도 가능
    public static QuizBalance of(Long quizId, BalanceSide side, String label, String imgUrl) {
        QuizBalance qb = new QuizBalance();
        qb.quizId = quizId;
        qb.side = side;
        qb.label = label;
        qb.imgUrl = imgUrl;
        return qb;
    }
}
