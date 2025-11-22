package com.example.quizley.repository;

import com.example.quizley.entity.balance.QuizBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizBalanceRepository extends JpaRepository<QuizBalance, Long> {

    // 특정 퀴즈의 선택지 2개 조회 (A, B 순서로)
    List<QuizBalance> findByQuizIdOrderBySideAsc(Long quizId);
}