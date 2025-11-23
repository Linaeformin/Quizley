package com.example.quizley.repository;

import com.example.quizley.entity.balance.BalanceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarRepository extends JpaRepository<BalanceAnswer, Long> {

    // 특정 유저가 응답한 날짜 목록을 문자열로 조회
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') FROM balance_answer WHERE user_id = :userId", nativeQuery = true)
    List<String> findAnsweredDateStringsByUserId(@Param("userId") Long userId);
}
