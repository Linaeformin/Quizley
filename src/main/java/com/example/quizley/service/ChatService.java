package com.example.quizley.service;

import com.example.quizley.config.claude.ChatClaudeGateway;
import com.example.quizley.domain.MessageOrigin;
import com.example.quizley.dto.quiz.ChatMessageResDto;
import com.example.quizley.dto.quiz.SentChatMessageResDto;
import com.example.quizley.entity.quiz.AiMessage;
import com.example.quizley.repository.AiChatRepository;
import com.example.quizley.repository.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


// 채팅 전송 서비스
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiChatRepository aiChatRepository;
    private final AiMessageRepository aiMessageRepository;
    private final ChatClaudeGateway chatClaudeGateway;

    @Transactional
    public SentChatMessageResDto chat(Long chatId, Long userId, String userMessage) {

        // 1) 채팅방 소유권 확인 + 조회
        var chat = aiChatRepository.findByChatIdAndUsers_UserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("chat.txt not found"));

        // 2) 직전 USER / 직전 AI 메시지 가져오기
        var lastUserOpt = aiMessageRepository
                .findTop1ByChatChatIdAndOriginOrderByCreatedAtDesc(chatId, MessageOrigin.USER);

        var lastAiOpt = aiMessageRepository
                .findTop1ByChatChatIdAndOriginOrderByCreatedAtDesc(chatId, MessageOrigin.AI);

        String previousUser = lastUserOpt.map(AiMessage::getContent).orElse(null);
        String previousAi   = lastAiOpt.map(AiMessage::getContent).orElse(null);

        // 기존 누적 요약
        String historySummary = chat.getSummary();

        // 3) Claude 호출
        var result = chatClaudeGateway.chatWithSummary(
                userMessage,
                previousUser,
                previousAi,
                historySummary
        );

        // 4) USER 저장
        AiMessage userMsg = new AiMessage();
        userMsg.setChat(chat);
        userMsg.setOrigin(MessageOrigin.USER);
        userMsg.setContent(userMessage);
        AiMessage savedUser = aiMessageRepository.save(userMsg);

        // 5) AI 저장
        AiMessage aiMsg = new AiMessage();
        aiMsg.setChat(chat);
        aiMsg.setOrigin(MessageOrigin.AI);
        aiMsg.setContent(result.answer());
        AiMessage savedAi = aiMessageRepository.save(aiMsg);

        // 6) 요약 누적
        String newLineSummary = result.summary();
        String updatedSummary =
                (historySummary == null || historySummary.isBlank())
                        ? newLineSummary
                        : historySummary + " / " + newLineSummary;

        chat.setSummary(updatedSummary);

        // 7) date 포맷
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("a hh:mm")
                .withLocale(Locale.KOREAN);

        ChatMessageResDto userDto = ChatMessageResDto.builder()
                .origin(savedUser.getOrigin())
                .message(savedUser.getContent())
                .date(savedUser.getCreatedAt().format(fmt))
                .build();

        ChatMessageResDto aiDto = ChatMessageResDto.builder()
                .origin(savedAi.getOrigin())
                .message(savedAi.getContent())
                .date(savedAi.getCreatedAt().format(fmt))
                .build();

        return SentChatMessageResDto.builder()
                .chatId(chatId)
                .userMessage(userDto)
                .aiMessage(aiDto)
                .summary(newLineSummary)
                .build();
    }
}
