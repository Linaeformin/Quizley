package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.dto.profile.ProfileResponseDto;
import com.example.quizley.dto.profile.ProfileUpdateRequestDto;
import com.example.quizley.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    // 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(profileService.getMyProfile(me));
    }

    // 내 게시글
    @GetMapping("/me/posts")
    public ResponseEntity<?> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.getMyPosts(me.getId()));
    }

    // 내 댓글
    @GetMapping("/me/comments")
    public ResponseEntity<?> getMyComments(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.getMyComments(me.getId()));
    }

    // 좋아요 누른 게시글
    @GetMapping("/me/likes")
    public ResponseEntity<?> getMyLikedPosts(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(profileService.getMyLikedPosts(me.getId()));
    }

    // 프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails me,
            @ModelAttribute ProfileUpdateRequestDto dto
    ) throws Exception {

        return ResponseEntity.ok(profileService.updateMyProfile(me.getId(), dto));
    }
}
