package com.example.quizley.config.claude;

public interface ClaudeClient {
    String call(String model, long maxTokens, double temperature, String userPrompt);
}

