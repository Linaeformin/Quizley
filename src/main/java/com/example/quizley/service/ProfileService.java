package com.example.quizley.service;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.domain.Origin;
import com.example.quizley.dto.profile.MyPostDto;
import com.example.quizley.dto.profile.ProfileResponseDto;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.QuizRepository;
import com.example.quizley.repository.UsersRepository;
import com.example.quizley.util.TimeFormatUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsersRepository usersRepository;
    private final QuizRepository quizRepository;   // ★ 추가됨

    // 내 프로필 정보 조회
    public ProfileResponseDto getMyProfile(UserDetails userDetails) {

        Long userId = ((CustomUserDetails) userDetails).getId();

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        int currentExp = user.getPoint();  // 경험치 = point
        int requiredExp = 1200;            // 임시 레벨업 기준 값

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .level(user.getLevel())
                .currentExp(currentExp)
                .requiredExp(requiredExp)
                .build();
    }


    // 내가 작성한 게시글 조회 (USER가 만든 Quiz 기반)
    @Transactional
    public List<MyPostDto> getMyPosts(Long userId) {

        List<Quiz> quizzes = quizRepository
                .findByUserIdAndOriginOrderByCreatedAtDesc(userId, Origin.USER);

        return quizzes.stream()
                .map(q -> MyPostDto.builder()
                        .quizId(q.getQuizId())
                        .content(q.getContent())
                        .category(q.getCategory().name())
                        .createdAt(TimeFormatUtil.formatTimeAgo(q.getCreatedAt()))
                        .isAnonymous(q.getIsAnonymous())
                        .build())
                .toList();
    }
}
