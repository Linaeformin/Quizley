package com.example.quizley.repository;

import com.example.quizley.entity.balance.BalanceAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository extends JpaRepository<BalanceAnswer, Long> {

    @Query("SELECT DISTINCT CAST(c.createdAt AS date) FROM AiChat c WHERE c.users.userId = :userId")
    List<Object> findAiChatDates(@Param("userId") Long userId);

    @Query("SELECT DISTINCT CAST(b.createdAt AS date) FROM BalanceAnswer b WHERE b.userId = :userId")
    List<Object> findBalanceDates(@Param("userId") Long userId);
}

