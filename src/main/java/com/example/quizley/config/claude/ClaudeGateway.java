package com.example.quizley.config.claude;

import java.util.Map;


// AI 응답을 key-value로 변환
public interface ClaudeGateway {
    // 결과 DTO
    record GenerateResult(String category, String content) {}
    // 6개 카테고리로 나누어서 반환
    Map<String, String> generateMapFromPrompt(String promptText);
}