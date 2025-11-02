package com.example.quizley.config.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient; // ★ 이거 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// 클로드 SDK 클라이언트를 스프링 빈으로 구성
@Configuration
public class AnthropicConfig {
    @Bean
    public AnthropicClient anthropicClient(@Value("${app.anthropic.api-key}") String apiKey){
        // OkHttp 클라이언트의 builder 사용
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}


