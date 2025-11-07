package com.example.quizley.config.claude;// ... imports 생략
import com.anthropic.client.AnthropicClient;

import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;


// ClaudeGateway 인터페이스 실제 구현
@Component
@RequiredArgsConstructor
public class ClaudeLiveGateway implements ClaudeGateway {

    // API 호출용 클라이언트
    private final AnthropicClient client;

    // JSON 직렬화를 위한 인스턴스
    private final ObjectMapper om = new ObjectMapper();

    // 사용한 모델이름과 토큰 제한 프로퍼티
    @Value("${app.anthropic.model}") private String model;
    @Value("${app.anthropic.max-tokens}") private Integer maxTokens;

    // 프롬프트 문자열을 받아 LLM 호출, 응답을 JSON-Map<String, String>으로 파싱
    @Override
    public Map<String, String> generateMapFromPrompt(String promptText) {
        var req = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens.longValue())
                .temperature(0.0)
                .addUserMessage(promptText)
                .build();

        // 클로드 API 호출
        Message res = client.messages().create(req);

        // 1) content[0].text 추출
        String joined = extractFirstText(res);
        if (joined.isBlank()) {
            throw new IllegalStateException("content[0].text 비어있음");
        }

        // 2) 코드펜스 제거 → 중괄호 블록만 추출
        String cleaned = stripFence(joined);
        String json    = extractJsonObject(cleaned);

        // 3) Map<String,String> 파싱
        Map<String,String> map = parseJsonMap(json);
        if (map.isEmpty()) throw new IllegalStateException("빈 JSON 응답");

        return map;
    }

    // content[0].text 추출
    private String extractFirstText(Message res) {
        try {
            // 응답 객체를 JSON 문자열로 직렬화
            String rawJson = om.writeValueAsString(res);

            // 문자열을 JsonNode 트리로 파싱
            var root = om.readTree(rawJson);

            // content 배열 노드 접근
            var content = root.path("content");

            // content[0].text가 있으면 문자열 반환
            if (content.isArray() && !content.isEmpty()) {
                return content.get(0).path("text").asText("");
            }
            // 없으면 빈 문자열 반환
            return "";

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("RAW 직렬화/파싱 실패: " + e.getMessage(), e);
        }
    }

    // Map<String,String> 파싱
    private Map<String,String> parseJsonMap(String json) {
        try {
            // 제네릭 타입 정보 유지하여 파싱
            return om.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String,String>>() {});

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 에러 파악을 위해 원본 JSON 일부를 잘라둠
            String cut = json == null ? "null" : (json.length()>300 ? json.substring(0,300)+"...(truncated)" : json);

            // 파싱 실패 반환
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage() + " / 원문=" + cut, e);
        }
    }

    // 영문 카테고리 키를 한글로 변환
    private static String stripFence(String s){
        // 카테고리가 없을 때
        if (s == null) return "";

        // 카테고리 추출
        String t = s.trim();

        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl > 0) t = t.substring(nl + 1);
            int end = t.lastIndexOf("```");
            if (end >= 0) t = t.substring(0, end);
            t = t.trim();
        }

        return t;
    }

    // 가장 바깥 중괄호 오브젝트 잘라내기
    private static String extractJsonObject(String s){
        if (s == null) return "";

        // 시작과 끝 변수
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');

        // 중괄호를 기준으로 오브젝트 잘라내기
        return (start >= 0 && end > start) ? s.substring(start, end + 1).trim() : s.trim();
    }
}

