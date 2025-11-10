package com.example.quizley.repository;

import com.example.quizley.entity.balance.BalanceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository extends JpaRepository<BalanceAnswer, Long> {

    // 특정 유저가 응답한 날짜 목록 가져오기
    @Query("SELECT DATE(b.createdAt) FROM BalanceAnswer b WHERE b.userId = :userId")
    List<LocalDate> findAnsweredDatesByUserId(@Param("userId") Long userId);
}