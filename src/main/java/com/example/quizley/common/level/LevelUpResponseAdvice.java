package com.example.quizley.common.level;

import com.example.quizley.dto.users.LevelUpResultDto;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.LinkedHashMap;
import java.util.Map;


// 컨트롤러 응답을 가로채서 원래 응답 데이터와 레벨업 데이터를 내려줌
@RestControllerAdvice
public class LevelUpResponseAdvice implements ResponseBodyAdvice<Object> {

    // 모든 응답에 적용
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 1) 메서드에 @LevelUpResponse 붙어 있는지
        boolean methodAnnotated = returnType.getMethodAnnotation(LevelUpResponse.class) != null;

        return methodAnnotated;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 이번 요청에서 레벨업이 있었는지 확인
        LevelUpResultDto levelUp = LevelUpContext.getAndClear();

        // 기존 응답 + 레벨업 정보를 하나의 JSON 구조로 감싸기
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("data", body);    // 기존 컨트롤러 응답 DTO
        wrapper.put("levelUp", levelUp); // 레벨업 DTO

        return wrapper;
    }
}
