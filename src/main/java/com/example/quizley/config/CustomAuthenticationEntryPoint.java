package com.example.quizley.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.quizley.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


// 인증되지 않은 사용자 처리
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om = new ObjectMapper();

    // 인증되지 않은 사용자 서비스 이용 시도 시 처리
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 에러 JSON에 넣을 데이터
        var entity = ApiError.of(401, "UNAUTHORIZED", "인증되지 않은 사용자입니다.");
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 에러 JSON 생성
        om.writeValue(response.getWriter(), entity.getBody());
    }
}
