package com.example.quizley.controller;

import com.example.quizley.dto.community.CommunityHomeResponse;
import com.example.quizley.dto.community.QuizListDto;
import com.example.quizley.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    //커뮤니티 홈 화면 조회 GET /api/community/home?date=2025-10-05
    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getCommunityHome(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        CommunityHomeResponse response = communityService.getCommunityHome(date);

        //응답 JSON 생성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("data", response);
        body.put("message", "커뮤니티 홈 화면 조회 성공");
        return ResponseEntity.ok(body);
    }

    //게시글 목록 조회(최신순/인기순)
    @GetMapping("/quizzes")
    public ResponseEntity<Map<String, Object>> getQuizList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "latest") String sortBy) {

        List<QuizListDto> quizzes = communityService.getQuizList(date, sortBy);

        //응답 JSON 생성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시글 목록 조회 성공");
        body.put("data", quizzes);

        return ResponseEntity.ok(body);
    }
}
