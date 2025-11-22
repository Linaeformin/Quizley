package com.example.quizley.service;

import com.example.quizley.dto.profile.ProfileResponseDto;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.UsersRepository;
import com.example.quizley.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsersRepository usersRepository;

    public ProfileResponseDto getMyProfile(UserDetails userDetails) {

        // CustomUserDetails → userId 얻기
        Long userId = ((CustomUserDetails) userDetails).getId();

        // userId로 Users 엔티티 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        int currentExp = user.getPoint();  // point = 경험치
        int requiredExp = 1200;            // 레벨업 기준(예시)

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .level(user.getLevel())
                .currentExp(currentExp)
                .requiredExp(requiredExp)
                .build();
    }
}
