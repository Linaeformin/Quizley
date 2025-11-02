package com.example.quizley.repository;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.QuizType;
import com.example.quizley.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

// 퀴즈 레포지토리
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 이미 존재하는 날짜에 등록된 질문인지
    boolean existsByTypeAndPublishedDateAndCategory(
            QuizType type,
            LocalDate publishedDate,
            Category category
    );
}

