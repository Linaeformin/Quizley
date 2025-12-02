package com.example.quizley.service;

import com.example.quizley.config.claude.ClaudeClient;
import com.example.quizley.config.claude.PromptLoader;
import com.example.quizley.config.claude.WeekdayPromptType;
import com.example.quizley.domain.ChatStatus;
import com.example.quizley.domain.CommentAnonymous;
import com.example.quizley.domain.MessageOrigin;
import com.example.quizley.domain.Status;
import com.example.quizley.dto.quiz.ChatInsightResDto;
import com.example.quizley.dto.quiz.ChatMessageResDto;
import com.example.quizley.dto.quiz.SentChatMessageResDto;
import com.example.quizley.dto.quiz.TopCommentDto;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.quiz.AiMessage;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.AiChatRepository;
import com.example.quizley.repository.AiMessageRepository;
import com.example.quizley.repository.CommentRepository;
import com.example.quizley.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


// 채팅 서비스
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiChatRepository aiChatRepository;
    private final AiMessageRepository aiMessageRepository;
    private final ClaudeClient claudeClient;
    private final PromptLoader promptLoader;
    private final CommentRepository commentRepository;
    private final UsersRepository usersRepository;

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

        // 한국 시간 기준 현재 시각
        LocalDateTime nowKst = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        ChatMessageResDto userDto = ChatMessageResDto.builder()
                .origin(savedUser.getOrigin())
                .message(savedUser.getContent())
                .date(nowKst.format(fmt))
                .build();

        ChatMessageResDto aiDto = ChatMessageResDto.builder()
                .origin(savedAi.getOrigin())
                .message(savedAi.getContent())
                .date(nowKst.format(fmt))
                .build();

        return SentChatMessageResDto.builder()
                .chatId(chatId)
                .userMessage(userDto)
                .aiMessage(aiDto)
                .summary(newLineSummary)
                .build();
    }

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

    // 누적 summary 합치기 + feedback 생성
    @Transactional
    public ChatInsightResDto summarizeAndFeedback(Long chatId, Long userId) {

        var chat = aiChatRepository.findByChatIdAndUsers_UserId(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("CHAT_NOT_FOUND"));

        var quiz = chat.getQuiz();

        // 날짜 포맷
        var dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
                .withLocale(Locale.KOREAN);

        String dateStr = quiz.getPublishedDate() != null
                ? quiz.getPublishedDate().format(dateFormatter)
                : "";

        // 1) topCommentDtoList 구성
        var popularComments = commentRepository
                .findInsightCommentsByQuizIdExceptUser(quiz.getQuizId(), userId);

        var top3 = popularComments.stream()
                .limit(3)
                .toList();

        var topCommentDtoList = top3.stream()
                .map(c -> {
                    TopCommentDto dto = new TopCommentDto();
                    dto.setCommentId(c.getCommentId());
                    dto.setComment(c.getContent());
                    return dto;
                })
                .toList();

        // 2) ChatStatus 에 따라 분기
        String finalSummary;
        String finalFeedback;

        if (chat.getChatStatus() == ChatStatus.CLOSED) {
            // 이미 닫힌 채팅방: AI 다시 호출하지 말고 DB 값 그대로 사용

            // summary 는 ai_chat.summary 에 최종본이 들어있다고 가정
            finalSummary = chat.getSummary();

            // feedback 은 comment.feedback 에 저장돼 있음
            finalFeedback = commentRepository
                    .findByQuiz_QuizIdAndUser_UserId(quiz.getQuizId(), userId)
                    .map(Comment::getFeedback)
                    .orElse(null);

        } else {
            // OPEN 이면: 지금 처음 요약/피드백 생성하는 상태

            String historySummary = chat.getSummary();
            if (historySummary == null || historySummary.isBlank()) {
                throw new IllegalStateException("요약 데이터가 없습니다.");
            }

            var summaries = Arrays.stream(historySummary.split(" / "))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (summaries.isEmpty()) {
                throw new IllegalStateException("요약 데이터가 없습니다.");
            }

            // 1) bot_summary.txt로 최종 요약본 생성
            String mergedSummary = callBotSummary(summaries).trim();

            // 2) feedback.txt로 피드백 생성
            String feedback = callFeedback(mergedSummary).trim();

            // 3) ai_chat.summary를 최종 요약본으로 덮어쓰기
            chat.setSummary(mergedSummary);

            // 4) Comment 생성 or 업데이트
                    Comment comment = commentRepository
                            .findByQuiz_QuizIdAndUser_UserId(quiz.getQuizId(), userId)
                            .orElseGet(() -> {
                                // 없으면 새로 생성
                                Comment c = new Comment();
                                Users user = usersRepository.getReferenceById(userId);

                                c.setUser(user);
                                c.setQuiz(quiz);

                                // 사용자의 "최종 답변"으로 ai 요약본 삽입
                                //c.setContent(mergedSummary);

                                c.setStatus(Status.PROGRESS);                     // 답변 완료 상태로 가정
                                c.setCommentAnonymous(CommentAnonymous.CLOSE); // 등록 전에는 비공개
                                c.setWriterAnonymous(false);                  // 기본은 익명 아님
                                c.setLikeCount(0);

                                return c;
                            });
            comment.setContent(mergedSummary);
            // 새로 만들었든 기존이든, feedback은 여기서 세팅
            comment.setFeedback(feedback);
            commentRepository.save(comment);

            // 5) 채팅방 상태를 CLOSED 로 변경
            chat.close();

            finalSummary = mergedSummary;
            finalFeedback = feedback;
        }

        // 3) 최종 DTO 조립
        ChatInsightResDto dto = new ChatInsightResDto();
        dto.setChatId(chat.getChatId());
        dto.setQuizId(quiz.getQuizId());
        dto.setCategory(quiz.getCategory().name());
        dto.setDate(dateStr);
        dto.setQuizName(quiz.getContent());
        dto.setSummary(finalSummary);
        dto.setFeedback(finalFeedback);
        dto.setTopCommentDtoList(topCommentDtoList);

        return dto;
    }


    // 여러 줄 summary → 하나의 자연스러운 요약본
    private String callBotSummary(List<String> summaries) {
        String systemPrompt = promptLoader.load(WeekdayPromptType.BOT_SUMMARY);

        StringBuilder sb = new StringBuilder();
        sb.append("[SYSTEM]\n").append(systemPrompt).append("\n\n");
        sb.append("[SUMMARY LIST]\n");

        for (int i = 0; i < summaries.size(); i++) {
            sb.append(i + 1).append(". ").append(summaries.get(i)).append("\n");
        }

        String finalPrompt = sb.toString();

        return claudeClient.call(
                summaryModel,
                summaryMaxTokens.longValue(),
                0.2,
                finalPrompt
        );
    }

    // 최종 요약본(= 사용자의 대답)에 대한 피드백
    private String callFeedback(String mergedSummary) {
        String systemPrompt = promptLoader.load(WeekdayPromptType.FEEDBACK);

        StringBuilder sb = new StringBuilder();
        sb.append("[SYSTEM]\n").append(systemPrompt).append("\n\n");
        sb.append("[USER ANSWER]\n").append(mergedSummary);

        String finalPrompt = sb.toString();

        return claudeClient.call(
                chatModel,
                chatMaxTokens.longValue(),
                0.3,
                finalPrompt
        );
    }
}
