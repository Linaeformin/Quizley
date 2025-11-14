package com.example.quizley.controller;

import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.service.CalendarService;
import com.example.quizley.config.jwt.JwtProvider;
import com.example.quizley.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final JwtProvider jwtProvider;
    private final UsersRepository usersRepository;

    // 로그인된 사용자의 캘린더 응답 기록 조회
    @GetMapping
    public ResponseEntity<CalendarResponseDto> getCalendar(
            @RequestHeader("Authorization") String token) {

        // JWT에서 로그인 ID 추출
        String jwt = token.replace("Bearer ", "");
        String loginId = jwtProvider.getSubject(jwt);

        // 로그인 ID로 유저 PK(userId) 조회
        Long userId = usersRepository.findById(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getUserId();

        // 캘린더 데이터 조회
        CalendarResponseDto response = calendarService.getCalendar(userId);
        return ResponseEntity.ok(response);
    }
}
