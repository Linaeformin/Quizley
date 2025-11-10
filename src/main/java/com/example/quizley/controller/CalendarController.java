package com.example.quizley.controller;

import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    // 특정 유저의 캘린더 응답 기록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<CalendarResponseDto> getCalendar(@PathVariable Long userId) {
        return ResponseEntity.ok(calendarService.getCalendar(userId));
    }
}