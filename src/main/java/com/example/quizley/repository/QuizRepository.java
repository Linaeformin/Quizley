package com.example.quizley.repository;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.QuizType;
import com.example.quizley.domain.Origin;
import com.example.quizley.entity.quiz.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// 퀴즈 레포지토리
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 이미 존재하는 날짜에 등록된 질문인지
    boolean existsByTypeAndPublishedDateAndCategory(
            QuizType type,
            LocalDate publishedDate,
            Category category
    );

    //커뮤니티 기능: 오늘의 질문 조회
    Optional<Quiz> findByPublishedDateAndOrigin(
            LocalDate publishedDate,
            Origin origin
    );

    //특정 날짜의 모든 질문 조회(최신순)
    List<Quiz> findByPublishedDateOrderByCreatedAtDesc(LocalDate publishedDate);

    //특정 날짜, 특정 카테고리의 질문 조회(최신순, 상위 N개)
    List<Quiz> findByPublishedDateAndCategoryOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Category category,
            Pageable pageable
    );

    //Origin 포함한 조회
    List<Quiz> findByPublishedDateAndOriginAndCategoryOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Origin origin,
            Category category,
            Pageable pageable
    );

    //특정 날짜, 특정 Origin의 질문 조회 (최신순)
    List<Quiz> findByPublishedDateAndOriginOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Origin origin
    );
}

