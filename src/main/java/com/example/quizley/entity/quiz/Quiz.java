package com.example.quizley.entity.quiz;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import com.example.quizley.domain.QuizType;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

// 퀴즈 엔티티
@Entity
@Table(name="quiz")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Origin origin;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private QuizType type;

    @Lob @Column(nullable=false)
    private String content;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Category category;

    private Long userId;
    private Boolean isAnonymous;

    @Column(nullable=false)
    private LocalDate publishedDate;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false)
    private LocalDateTime modifiedAt;

    @PrePersist void pre(){
        var now = LocalDateTime.now();
        if (createdAt==null) createdAt = now;
        if (modifiedAt==null) modifiedAt = now;
    }

    @PreUpdate void up(){ modifiedAt = LocalDateTime.now(); }
}