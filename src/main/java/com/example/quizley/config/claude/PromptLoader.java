package com.example.quizley.config.claude;


// 폴더명 기준 프롬프트 텍스트를 읽어옴
public interface PromptLoader {
    String load(String tag);
}
