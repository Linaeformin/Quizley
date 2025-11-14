package com.example.quizley.controller;

import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.dto.insight.InsightRecordResponseDto;
import com.example.quizley.service.InsightRecordService;
import com.example.quizley.service.CalendarService;
import com.example.quizley.config.jwt.JwtProvider;
import com.example.quizley.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/insight/record")
@RequiredArgsConstructor
public class InsightRecordController {

    private final InsightRecordService insightRecordService;
    private final CalendarService calendarService;
    private final JwtProvider jwtProvider;
    private final UsersRepository usersRepository;

    // 선택한 날짜의 인사이트 조회
    @GetMapping("/{date}")
    public ResponseEntity<InsightRecordResponseDto> getInsightByDate(
            @PathVariable LocalDate date,
            HttpServletRequest request
    ) {
        // JWT에서 로그인 ID 추출
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build(); // 인증 실패
        }

        String jwt = token.replace("Bearer ", ""); // 실제 JWT 값만 분리
        String loginId = jwtProvider.getSubject(jwt); // 토큰에서 로그인 ID 추출

        // 로그인 ID로 유저 PK 조회
        Long userId = usersRepository.findById(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getUserId();

        // 인사이트 조회
        InsightRecordResponseDto response = insightRecordService.getInsightByDate(date);
        return ResponseEntity.ok(response);
    }

    // 선택한 인사이트 삭제
    @DeleteMapping("/{quizId}")
    public ResponseEntity<CalendarResponseDto> deleteInsight(
            @PathVariable Long quizId,
            HttpServletRequest request
    ) {
        // JWT에서 로그인 ID 추출
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String jwt = token.replace("Bearer ", "");
        String loginId = jwtProvider.getSubject(jwt);

        // 로그인 ID로 유저 PK 조회
        Long userId = usersRepository.findById(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getUserId();

        // 인사이트 삭제 + 캘린더 갱신
        insightRecordService.deleteInsight(quizId, userId);
        CalendarResponseDto updatedCalendar = calendarService.getCalendar(userId);
        return ResponseEntity.ok(updatedCalendar);
    }
}
