package com.example.quizley.repository;

import com.example.quizley.entity.quiz.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


// 채팅 메시지 레포지토리
@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByChat_ChatIdOrderByCreatedAtAsc(Long chatId);
}

