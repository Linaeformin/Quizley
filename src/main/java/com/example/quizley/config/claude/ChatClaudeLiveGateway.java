package com.example.quizley.config.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static com.example.quizley.config.claude.WeekdayPromptType.*;


// 채팅 응답 게이트웨이
@Component
@RequiredArgsConstructor
public class ChatClaudeLiveGateway implements ChatClaudeGateway {

    private final AnthropicClient client;
    private final PromptLoader promptLoader;

    // JSON 파싱용
    private final ObjectMapper om = new ObjectMapper();

    // 채팅/요약에 쓸 모델과 토큰 설정
    @Value("${app.anthropic.chat.txt-model:${app.anthropic.model}}")
    private String chatModel;

    @Value("${app.anthropic.summary.txt-model:${app.anthropic.model}}")
    private String summaryModel;

    @Value("${app.anthropic.chat.txt-max-tokens:1024}")
    private Integer chatMaxTokens;

    @Value("${app.anthropic.summary.txt-max-tokens:256}")
    private Integer summaryMaxTokens;

    // ChatClaudeGateway 구현 메서드
    @Override
    public ChatResult chatWithSummary(
            String currentUserMessage,
            String previousUserMessage,
            String previousAiMessage,
            String historySummary
    ) {

        // 시스템 프롬프트 로드
        String chatSystemPrompt = promptLoader.load(CHAT);
        String summarySystemPrompt = promptLoader.load(SUMMARY);

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

        // 3) 결과 반환
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

        // 클로드 메시지 생성 요청
        MessageCreateParams req = MessageCreateParams.builder()
                .model(chatModel)
                .maxTokens(chatMaxTokens.longValue())
                .temperature(0.3)
                .addUserMessage(finalPrompt)
                .build();

        // 클로드 호출
        Message res = client.messages().create(req);

        // 첫 번째 content의 text를 추출
        return extractFirstText(res);
    }

    // 요약 메서드
    private String callSummary(
            String systemPrompt,
            String textToSummarize
    ) {
        StringBuilder sb = new StringBuilder();

        // 요약 전용 system 규칙을 상단에 두고 아래에 요약 대상 텍스트를 붙임
        sb.append("[SYSTEM]\n").append(systemPrompt).append("\n\n");
        sb.append("[TEXT]\n").append(textToSummarize);

        // 최종 프롬프트 생성
        String finalPrompt = sb.toString();

        // 요약용 모델 요청
        MessageCreateParams req = MessageCreateParams.builder()
                .model(summaryModel)
                .maxTokens(summaryMaxTokens.longValue())
                .temperature(0.0)
                .addUserMessage(finalPrompt)
                .build();

        // 클로드 호출
        Message res = client.messages().create(req);

        // 첫 번째 content의 text를 추출하고 양쪽 공백 제거
        return extractFirstText(res).trim();
    }

    // 요약용 텍스트 구성
    private String buildSummaryInput(
            String currentUserMessage,
            String previousAiMessage
    ) {
        StringBuilder sb = new StringBuilder();

        // 이전 AI 응답이 있으면 먼저 붙이기
        if (previousAiMessage != null && !previousAiMessage.isBlank()) {
            sb.append("[이전 AI 응답]\n").append(previousAiMessage).append("\n\n");
        }

        // 현재 사용자 메시지 붙이기
        sb.append("[현재 사용자 메시지]\n").append(currentUserMessage);

        // String으로 메시지 반환
        return sb.toString();
    }

    // content[0].text 추출
    private String extractFirstText(Message res) {
        try {
            // 메시지 객체를 JSON 문자열로 직렬화
            String rawJson = om.writeValueAsString(res);

            // JSON 문자열을 JsonNode로 파싱
            JsonNode root = om.readTree(rawJson);
            JsonNode content = root.path("content");

            // content의 첫 번쨰 text 필드 반환
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText("");
            }

            // content가 비어있거나 배열이 아니면 빈 문자열 반환
            return "";
        } catch (JsonProcessingException e) {
            // 직렬화 및 파싱 중 에러 발생 시 예외 처리
            throw new RuntimeException("RAW 직렬화/파싱 실패: " + e.getMessage(), e);
        }
    }
}
