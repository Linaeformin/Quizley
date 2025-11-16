package com.example.quizley.config.claude;

// 클로드 채팅 + 요약 결과를 감싸는 게이트웨이
public interface ChatClaudeGateway {

    // 채팅 결과를 담는 레코드 (answer : 클로드 답변, summary : 중간 요약)
    record ChatResult(String answer, String summary) {}

    // 채팅 메시지 및 요약본
    ChatResult chatWithSummary(
            String currentUserMessage,
            String previousUserMessage,
            String previousAiMessage,
            String historySummary
    );
}
