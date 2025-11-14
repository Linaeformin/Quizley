package com.example.quizley.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;import com.example.quizley.entity.quiz.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


// 채팅 메시지 레포지토리
@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    // 페이지 수 및 채팅방 ID로 채팅 메시지 반환
    Page<AiMessage> findByChatChatId(Long chatId, Pageable pageable);

    // 특정 채팅방의 전체 메시지 개수 반환
    long countByChatChatId(Long chatId);
}

