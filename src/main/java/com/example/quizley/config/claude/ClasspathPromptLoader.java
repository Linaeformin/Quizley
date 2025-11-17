package com.example.quizley.config.claude;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;


// 프롬포트 로드
@Component
public class ClasspathPromptLoader implements PromptLoader {

    @Override
    public String load(WeekdayPromptType type) {
        // 파일 이름으로 조회
        String path = "prompts/weekday/" + type.fileName() + ".txt";

        try (var is = new ClassPathResource(path).getInputStream()) {
            // InputStream 전체를 UTF-8 문자열로 변환 후 반환
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 파일이 없거나 읽기 실패 시 RuntimeException으로 예외 처리
            throw new RuntimeException("프롬프트 로드 실패: " + path, e);
        }
    }
}
