package com.example.quizley.repository;

import com.example.quizley.entity.insight.InsightAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsightAnswerRepository extends JpaRepository<InsightAnswer, Long> {

    // 같은 질문에 대한 해당 유저의 과거 답변 히스토리
    List<InsightAnswer> findByUserIdAndQuizIdOrderByCreatedAtDesc(Long userId, Long quizId);
}
