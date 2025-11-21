package com.example.quizley.service;

import com.example.quizley.config.claude.ClaudeClient;
import com.example.quizley.config.claude.PromptLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.quizley.config.claude.WeekdayPromptType.QUIZ;

@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final PromptLoader promptLoader;
    private final ClaudeClient claudeClient;
    private final QuizService quizService;

    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.anthropic.model}")
    private String model;

    @Value("${app.anthropic.max-tokens}")
    private Integer maxTokens;

    /**
     * 1) QUIZ 프롬프트 로드
     * 2) Anthropic 호출 → JSON 문자열(또는 코드블록) 받기
     * 3) Map<String,String>으로 파싱
     * 4) QuizService.saveSystemWeekdayBulk(map) 호출
     */
    @Transactional
    public int generateWeekdayQuizFromAi() {
        // 1) 프롬프트 로드
        String prompt = promptLoader.load(QUIZ);

        // 2) LLM 호출
        String joined = claudeClient.call(model, maxTokens.longValue(), 0.0, prompt);
        if (joined == null || joined.isBlank()) {
            throw new IllegalStateException("Anthropic 응답이 비어 있습니다.");
        }

        // 3) 코드펜스 제거 + JSON 오브젝트 추출
        String cleaned = stripFence(joined);
        String json = extractJsonObject(cleaned);

        // 4) Map<String,String> 파싱
        Map<String,String> map = parseJsonMap(json);
        if (map.isEmpty()) {
            throw new IllegalStateException("퀴즈 JSON 응답이 비어 있습니다.");
        }

        // 5) 기존 QuizService의 bulk 메서드 재사용
        return quizService.saveSystemWeekdayBulk(map);
    }

    // ===== 아래는 예전 ClaudeLiveGateway에서 가져온 유틸 메서드들 =====

    private Map<String,String> parseJsonMap(String json) {
        try {
            return om.readValue(json, new TypeReference<Map<String,String>>() {});
        } catch (JsonProcessingException e) {
            String cut = json == null ? "null" :
                    (json.length() > 300 ? json.substring(0,300) + "...(truncated)" : json);
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage() + " / 원문=" + cut, e);
        }
    }

    private static String stripFence(String s){
        if (s == null) return "";
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

    private static String extractJsonObject(String s){
        if (s == null) return "";
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        return (start >= 0 && end > start) ? s.substring(start, end + 1).trim() : s.trim();
    }
}
