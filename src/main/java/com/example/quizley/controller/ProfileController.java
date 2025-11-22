package com.example.quizley.controller;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.dto.profile.ProfileResponseDto;
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

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();

        ProfileResponseDto response = profileService.getMyProfile(me);
        return ResponseEntity.ok(response);
    }

    // 내가 작성한 게시글 조회
    @GetMapping("/me/posts")
    public ResponseEntity<?> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails me) {

        if (me == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(profileService.getMyPosts(me.getId()));
    }
}
