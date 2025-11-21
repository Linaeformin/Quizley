package com.example.quizley.service;

import com.example.quizley.config.claude.ClaudeClient;
import com.example.quizley.config.claude.PromptLoader;
import com.example.quizley.config.claude.WeekdayPromptType;
import com.example.quizley.domain.MessageOrigin;
import com.example.quizley.dto.quiz.ChatMessageResDto;
import com.example.quizley.dto.quiz.SentChatMessageResDto;
import com.example.quizley.entity.quiz.AiMessage;
import com.example.quizley.repository.AiChatRepository;
import com.example.quizley.repository.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiChatRepository aiChatRepository;
    private final AiMessageRepository aiMessageRepository;
    private final ClaudeClient claudeClient;
    private final PromptLoader promptLoader;

    // 채팅/요약에 쓸 모델과 토큰 설정
    @Value("${app.anthropic.chat.txt-model:${app.anthropic.model}}")
    private String chatModel;

    @Value("${app.anthropic.summary.txt-model:${app.anthropic.model}}")
    private String summaryModel;

    @Value("${app.anthropic.chat.txt-max-tokens:1024}")
    private Integer chatMaxTokens;

    @Value("${app.anthropic.summary.txt-max-tokens:256}")
    private Integer summaryMaxTokens;

    // 기존 ChatClaudeGateway.ChatResult 를 여기로 옮김
    public record ChatResult(String answer, String summary) {}

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

        // 3) Claude 호출 (이제는 ChatService 안 메서드로)
        ChatResult result = chatWithSummary(
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

    // ====== 여기부터는 예전 ChatClaudeLiveGateway 로직을 옮겨온 부분 ======

    // 채팅 + 한줄 요약 생성
    private ChatResult chatWithSummary(
            String currentUserMessage,
            String previousUserMessage,
            String previousAiMessage,
            String historySummary
    ) {

        // 시스템 프롬프트 로드
        String chatSystemPrompt = promptLoader.load(WeekdayPromptType.CHAT);
        String summarySystemPrompt = promptLoader.load(WeekdayPromptType.SUMMARY);

        // 1) 답변 생성
        String answer = callChat(
                chatSystemPrompt,
                currentUserMessage,
                historySummary,
                previousUserMessage,
                previousAiMessage
        );

        // 2) "이전 AI + 현재 USER" 기준 한 줄 요약
        String summaryInput = buildSummaryInput(currentUserMessage, previousAiMessage);
        String oneLineSummary = callSummary(summarySystemPrompt, summaryInput);

        return new ChatResult(answer, oneLineSummary);
    }

    // 클로드 채팅 모델 호출 및 실제 답변 생성
    private String callChat(
            String systemPrompt,
            String currentUserMessage,
            String historySummary,
            String previousUserMessage,
            String previousAiMessage
    ) {
        StringBuilder sb = new StringBuilder();

        // 시스템 블록에 프롬프트 추가
        sb.append("[SYSTEM]\n").append(systemPrompt).append("\n\n");

        // 이전 요약이 있으면 이전 요약 블록으로 추가
        if (historySummary != null && !historySummary.isBlank()) {
            sb.append("[HISTORY SUMMARY]\n").append(historySummary).append("\n\n");
        } else {
            // 요약이 아직 없다면, 원문 이전 메시지를 직접 넣어줌
            if (previousAiMessage != null && !previousAiMessage.isBlank()) {
                sb.append("[PREVIOUS AI]\n").append(previousAiMessage).append("\n\n");
            }
            if (previousUserMessage != null && !previousUserMessage.isBlank()) {
                sb.append("[PREVIOUS USER]\n").append(previousUserMessage).append("\n\n");
            }
        }

        // 현재 사용자 메시지를 블록으로 넣어줌
        sb.append("[CURRENT USER]\n").append(currentUserMessage);

        String finalPrompt = sb.toString();

        // 이제 AnthropicClient 직접 안 쓰고 ClaudeClient 사용
        return claudeClient.call(
                chatModel,
                chatMaxTokens.longValue(),
                0.3,
                finalPrompt
        );
    }

    // 요약 메서드
    private String callSummary(
            String systemPrompt,
            String textToSummarize
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("[SYSTEM]\n").append(systemPrompt).append("\n\n");
        sb.append("[TEXT]\n").append(textToSummarize);

        String finalPrompt = sb.toString();

        return claudeClient.call(
                summaryModel,
                summaryMaxTokens.longValue(),
                0.0,
                finalPrompt
        ).trim();
    }

    // 요약용 텍스트 구성
    private String buildSummaryInput(
            String currentUserMessage,
            String previousAiMessage
    ) {
        StringBuilder sb = new StringBuilder();

        if (previousAiMessage != null && !previousAiMessage.isBlank()) {
            sb.append("[이전 AI 응답]\n").append(previousAiMessage).append("\n\n");
        }

        sb.append("[현재 사용자 메시지]\n").append(currentUserMessage);
        return sb.toString();
    }
}
