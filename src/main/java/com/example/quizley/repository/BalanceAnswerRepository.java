package com.example.quizley.repository;

import com.example.quizley.entity.balance.BalanceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BalanceAnswerRepository extends JpaRepository<BalanceAnswer, Long> {

    // 사용자가 특정 날짜에 푼 퀴즈 조회
    List<BalanceAnswer> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 특정 퀴즈 + 유저의 답변 삭제
    void deleteByQuizIdAndUserId(Long quizId, Long userId);

    // 특정 퀴즈의 모든 투표 결과 조회
    List<BalanceAnswer> findByQuizId(Long quizId);
}

