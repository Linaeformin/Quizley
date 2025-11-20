package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.service.CalendarService;
import com.example.quizley.config.jwt.JwtProvider;
import com.example.quizley.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserDetails me)
    throws Exception {
        // 권한이 있을 때
        if(me == null) return ResponseEntity.status(401).build();

        // 캘린더 데이터 조회
        CalendarResponseDto response = calendarService.getCalendar(me.getId());
        return ResponseEntity.ok(response);
    }
}
