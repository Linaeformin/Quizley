package com.example.quizley.repository;

import com.example.quizley.domain.MessageOrigin;
import com.example.quizley.entity.quiz.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


// 채팅 메시지 레포지토리
@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    // 페이지 수 및 채팅방 ID로 채팅 메시지 반환
    Page<AiMessage> findByChatChatId(Long chatId, Pageable pageable);

    // 특정 채팅방의 전체 메시지 개수 반환
    long countByChatChatId(Long chatId);

    // 1) 특정 chat의 전체 히스토리 (정렬까지 포함)
    List<AiMessage> findByChatChatIdOrderByCreatedAtAsc(Long chatId);

    // 2) 특정 chat에서 가장 최근 메시지 1개 (USER/AI 공통)
    Optional<AiMessage> findTop1ByChatChatIdOrderByCreatedAtDesc(Long chatId);

    // 3) 특정 chat에서 origin 기준 가장 최근 메시지 1개
    Optional<AiMessage> findTop1ByChatChatIdAndOriginOrderByCreatedAtDesc(Long chatId, MessageOrigin origin);

    // 4) 특정 chat의 모든 AI 메시지 (필요하면)
    List<AiMessage> findByChatChatIdAndOriginOrderByCreatedAtAsc(Long chatId, MessageOrigin origin);
}
