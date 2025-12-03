package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.domain.Category;
import com.example.quizley.dto.community.*;
import com.example.quizley.service.CommunityDetailService;
import com.example.quizley.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import com.example.quizley.common.level.LevelUpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final CommunityDetailService communityDetailService;

    //커뮤니티 홈 화면 조회
    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getCommunityHome(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Category category,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? (long) userDetails.getId() : null;
        CommunityHomeResponse response = communityService.getCommunityHome(date, category, currentUserId);

        //응답 JSON 생성(데이터 필드가 필요하므로 직접 Map 구성)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("data", response);
        body.put("message", "커뮤니티 홈 화면 조회 성공");
        return ResponseEntity.ok(body);
    }

    //게시글 목록 조회
    @GetMapping("/quizzes")
    public ResponseEntity<Map<String, Object>> getQuizList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam Category category,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? (long) userDetails.getId() : null;
        List<QuizListDto> quizzes = communityService.getQuizList(date, sortBy, category, currentUserId);

        //응답 JSON 생성(데이터 필드가 필요하므로 직접 Map 구성)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시글 목록 조회 성공");
        body.put("data", quizzes);

        return ResponseEntity.ok(body);
    }

    // 게시글 상세 조회
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<Map<String, Object>> getQuizDetail(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "latest") String sort, // 기본값 latest
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? (long) userDetails.getId() : null;

        QuizDetailResponse response = communityDetailService.getQuizDetail(quizId, currentUserId, sort);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시글 상세 조회 성공");
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    // 주말 퀴즈 상세 조회
    @GetMapping("/weekend/quiz/{quizId}")
    public ResponseEntity<Map<String, Object>> getWeekendQuizDetail(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인 체크
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long currentUserId = (long) userDetails.getId();

        WeekendQuizDetailResponse response = communityDetailService.getWeekendQuizDetail(
                quizId, currentUserId, sort);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "주말 퀴즈 상세 조회 성공");
        body.put("data", response);
        return ResponseEntity.ok(body);
    }

    // 퀴즈 좋아요 선택
    @PostMapping("/quiz/{quizId}/like")
    public ResponseEntity<Map<String, Object>> selectQuizLike(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.selectQuizLike(quizId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "좋아요 처리 완료");
        return ResponseEntity.ok(body);
    }

    // 댓글 작성
    @LevelUpResponse
    @PostMapping("/quiz/{quizId}/comment")
    public ResponseEntity<Map<String, Object>> createComment(
            @PathVariable Long quizId,
            @RequestBody CommentCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long commentId = communityDetailService.createComment(quizId, dto, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 201);
        body.put("message", "댓글 작성 완료");
        body.put("commentId", commentId);

        return ResponseEntity.status(201).body(body);
    }

    // 댓글 좋아요 선택 기능
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<Map<String, Object>> selectCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.selectCommentLike(commentId, (long)userDetails.getId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "좋아요 처리 완료");
        return ResponseEntity.ok(body);
    }

    // 게시글 작성
    @LevelUpResponse
    @PostMapping("/quiz")
    public ResponseEntity<Map<String, Object>> createQuiz(
            @RequestBody @Valid QuizCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long quizId = communityDetailService.createUserQuiz(dto, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 201);
        body.put("message", "게시글 작성 완료");
        body.put("quizId", quizId);

        return ResponseEntity.status(201).body(body);
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.deleteComment(commentId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "댓글 삭제 완료");
        return ResponseEntity.ok(body);
    }

    // 다른 유저의 댓글 신고
    @PostMapping("/comment/{commentId}/report")
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.reportComment(commentId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "댓글 신고 완료");
        return ResponseEntity.ok(body);
    }

    // 사용자 차단
    @PostMapping("/user/{userId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.blockUser(userId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "사용자 차단 완료");
        return ResponseEntity.ok(body);
    }

    // 퀴즈 검색
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchQuizzes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long currentUserId = (userDetails != null) ? (long) userDetails.getId() : null;
        QuizSearchResponse response = communityService.searchQuizzes(keyword, sortBy, currentUserId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "퀴즈 검색 성공");
        body.put("data", response);

        return ResponseEntity.ok(body);
    }

    // 게시물 수정
    @PutMapping("/quiz/{quizId}")
    public ResponseEntity<Map<String, Object>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.updateUserQuiz(quizId, dto, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시물 수정 완료");
        return ResponseEntity.ok(body);
    }

    // 게시물 삭제
    @DeleteMapping("/quiz/{quizId}")
    public ResponseEntity<Map<String, Object>> deleteQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.deleteUserQuiz(quizId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시물 삭제 완료");
        return ResponseEntity.ok(body);
    }

    // 게시물 신고
    @PostMapping("/quiz/{quizId}/report")
    public ResponseEntity<Map<String, Object>> reportQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        communityDetailService.reportQuiz(quizId, (long) userDetails.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "게시물 신고 완료");
        return ResponseEntity.ok(body);
    }

    // 주말 커뮤니티 홈 화면 조회
    @GetMapping("/weekend/home")
    public ResponseEntity<Map<String, Object>> getWeekendCommunityHome(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Category category,
            @RequestParam(defaultValue = "latest") String sortBy,  // 추가
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long currentUserId = (userDetails != null) ? (long) userDetails.getId() : null;
        WeekendCommunityHomeResponse response = communityService.getWeekendCommunityHome(date, category, sortBy, currentUserId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("data", response);
        body.put("message", "주말 커뮤니티 홈 화면 조회 성공");
        return ResponseEntity.ok(body);
    }
}
