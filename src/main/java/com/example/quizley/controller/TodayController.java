package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.config.claude.ChatClaudeGateway;
import com.example.quizley.domain.Category;
import com.example.quizley.dto.quiz.ChatRoomFormDto;
import com.example.quizley.dto.quiz.ChatRoomResDto;
import com.example.quizley.dto.quiz.SentChatMessageFormDto;
import com.example.quizley.dto.quiz.SentChatMessageResDto;
import com.example.quizley.service.ChatService;
import com.example.quizley.service.QuizService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;


// 오늘의 퀴즈
@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
public class TodayController {

    private final QuizService quizService;
    private final ChatService chatService;

    // 오늘의 퀴즈 조회
    @GetMapping(value = "")
    public ResponseEntity<?> findTodayQuiz(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam Category category
    ) throws Exception {
        // 권한이 없을 때
        if (me == null) return ResponseEntity.status(401).build();

        // 카테고리가 비어있을 때
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }

        // 오늘 날짜 조회
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DayOfWeek dow = today.getDayOfWeek();

        // 주말 여부 확인
        boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

        // 평일이라면
        if (!weekend) {
            return ResponseEntity.ok(quizService.getWeekdayQuiz(today, String.valueOf(category), me.getId()));
        }

        // TODO : 주말일 때 데이터 반환
        return ResponseEntity.ok(quizService.getWeekdayQuiz(today, String.valueOf(category), me.getId()));
    }

    // 평일 오늘의 퀴즈 AI 채팅방 생성
    @PostMapping(value = "/chatroom")
    public ResponseEntity<?> createChatRoom(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody ChatRoomFormDto dto
    ) throws Exception {
        // 권한이 없을 때
        if (me == null) return ResponseEntity.status(401).build();

        // 채팅방 생성 서비스 출력 및 채팅방 PK 저장
        Long chatId = quizService.createChatRoom(dto, me.getId());

        // 채팅방 PK JSON 반환
        return ResponseEntity.ok(Map.of("chatId", chatId));
    }

    // 평일 오늘의 퀴즈 AI 채팅방 메시지 조회
    @GetMapping("/{chatId}")
    public ResponseEntity<?> getChatMessage(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "-1") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws Exception {
        // 권한이 없을 때
        if (me == null) {
            return ResponseEntity.status(401).build();
        }

        // 채팅방 및 채팅 메시지 데이터 반환
        ChatRoomResDto res = quizService.getMessage(chatId, me.getId(), page, size);

        return ResponseEntity.ok(res);
    }

    // 메시지 전송
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<SentChatMessageResDto> chatWithAi(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long chatId,
            @RequestBody SentChatMessageFormDto request
    ) {
        // 권한이 없을 때
        if (me == null) {
            return ResponseEntity.status(401).build();
        }

        // 유저가 보낸 메시지
        String userMessage = request.getMessage();

        // 채팅 메시지 결과 생성
        SentChatMessageResDto result = chatService.chat(chatId, me.getId(), userMessage);

        return ResponseEntity.ok(result);
    }
}
