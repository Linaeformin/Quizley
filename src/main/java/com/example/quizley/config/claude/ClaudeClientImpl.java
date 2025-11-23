package com.example.quizley.config.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClaudeClientImpl implements ClaudeClient {

    private final AnthropicClient client;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public String call(String model, long maxTokens, double temperature, String userPrompt) {
        var req = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .addUserMessage(userPrompt)
                .build();

        Message res = client.messages().create(req);
        return extractFirstText(res);
    }

    private String extractFirstText(Message res) {
        try {
            String rawJson = om.writeValueAsString(res);
            var root = om.readTree(rawJson);
            var content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText("");
            }
            return "";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RAW 직렬화/파싱 실패: " + e.getMessage(), e);
        }
    }
}

