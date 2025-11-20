package com.example.quizley.repository;

import com.example.quizley.entity.balance.BalanceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceAnswerRepository extends JpaRepository<BalanceAnswer, Long> {

    // 사용자가 특정 날짜에 푼 퀴즈 조회
    List<BalanceAnswer> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // 특정 퀴즈 + 유저의 답변 삭제
    void deleteByQuizIdAndUserId(Long quizId, Long userId);

    // 같은 질문에 다시 답해보기: 해당 유저가 이 퀴즈에 남긴 답변 히스토리
    List<BalanceAnswer> findByUserIdAndQuizIdOrderByCreatedAtDesc(Long userId, Long quizId);

    // 최신 기록(가장 마지막에 쓴 답변) – side 복사용
    Optional<BalanceAnswer> findTop1ByUserIdAndQuizIdOrderByCreatedAtDesc(Long userId, Long quizId);

    // 특정 퀴즈의 모든 투표 결과 조회
    List<BalanceAnswer> findByQuizId(Long quizId);
}

