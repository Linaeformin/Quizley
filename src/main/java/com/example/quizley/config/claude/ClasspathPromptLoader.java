package com.example.quizley.config.claude;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;


// 프롬프트.txt를 읽어 문자열로 반환
@Component
public class ClasspathPromptLoader implements PromptLoader {
    @Override public String load(String tag){

        // 폴더명(tag)를 기준으로 문자열로 반환
        try (var is = new ClassPathResource("prompts/" + tag + "/quiz.txt").getInputStream()){
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e){
            throw new RuntimeException("프롬프트 로드 실패: " + tag, e);
        }
    }
}

