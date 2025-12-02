package com.example.quizley.service;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.domain.Origin;
import com.example.quizley.dto.profile.*;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.quiz.QuizLike;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.CommentRepository;
import com.example.quizley.repository.QuizLikeRepository;
import com.example.quizley.repository.QuizRepository;
import com.example.quizley.repository.UsersRepository;
import com.example.quizley.storage.S3Service;
import com.example.quizley.util.TimeFormatUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UsersRepository usersRepository;
    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;
    private final QuizLikeRepository quizLikeRepository;
    private final PasswordEncoder passwordEncoder;  // 비밀번호 인코딩
    private final S3Service s3Service;

    // 내 프로필 조회
    public ProfileResponseDto getMyProfile(UserDetails userDetails) {

        Long userId = ((CustomUserDetails) userDetails).getId();

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        int currentExp = user.getPoint();
        int requiredExp = 1200;

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .level(user.getLevel())
                .currentExp(currentExp)
                .requiredExp(requiredExp)
                .build();
    }

    // 내가 작성한 게시글
    @Transactional
    public List<MyPostDto> getMyPosts(Long userId) {
        List<Quiz> quizzes = quizRepository
                .findByUserIdAndOriginOrderByCreatedAtDesc(userId, Origin.USER);

        return quizzes.stream()
                .map(q -> MyPostDto.builder()
                        .quizId(q.getQuizId())
                        .content(q.getContent())
                        .category(q.getCategory() != null ? q.getCategory().name() : "밸런스")
                        .createdAt(TimeFormatUtil.formatTimeAgo(q.getCreatedAt()))
                        .isAnonymous(q.getIsAnonymous())
                        .commentCount((int) commentRepository.countByQuiz_QuizId(q.getQuizId()))
                        .likeCount((int) quizLikeRepository.countByQuiz_QuizId(q.getQuizId()))
                        .build())
                .toList();
    }

    // 내가 작성한 댓글
    @Transactional
    public List<MyCommentDto> getMyComments(Long userId) {

        List<Comment> comments = commentRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userId);

        return comments.stream()
                .map(c -> {
                    Quiz q = c.getQuiz();

                    String author;
                    if (q.getOrigin() == Origin.SYSTEM) author = "퀴즐리봇";
                    else author = q.getIsAnonymous()
                            ? "익명"
                            : usersRepository.findById(q.getUserId())
                            .map(Users::getNickname)
                            .orElse("탈퇴한 사용자");

                    return MyCommentDto.builder()
                            .commentId(c.getCommentId())
                            .quizId(q.getQuizId())
                            .content(c.getContent())
                            .createdAt(TimeFormatUtil.formatTimeAgo(c.getCreatedAt()))
                            .author(author)
                            .likeCount(c.getLikeCount()) // 댓글 좋아요 카운트
                            .build();
                })
                .toList();
    }

    // 좋아요 누른 게시글
    @Transactional
    public List<MyLikedPostDto> getMyLikedPosts(Long userId) {

        List<QuizLike> likes = quizLikeRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userId);

        return likes.stream()
                .map(l -> {
                    Quiz q = l.getQuiz();

                    String author;
                    if (q.getOrigin() == Origin.SYSTEM) author = "퀴즐리봇";
                    else author = q.getIsAnonymous()
                            ? "익명"
                            : usersRepository.findById(q.getUserId())
                            .map(Users::getNickname)
                            .orElse("탈퇴한 사용자");

                    return MyLikedPostDto.builder()
                            .quizId(q.getQuizId())
                            .content(q.getContent())
                            .category(q.getCategory() != null ? q.getCategory().name() : "밸런스")
                            .createdAt(TimeFormatUtil.formatTimeAgo(q.getCreatedAt()))
                            .author(author)
                            .commentCount((int) commentRepository.countByQuiz_QuizId(q.getQuizId()))
                            .likeCount((int) quizLikeRepository.countByQuiz_QuizId(q.getQuizId()))
                            .build();
                })
                .toList();
    }

    // 프로필 수정
    @Transactional
    public ProfileResponseDto updateMyProfile(Long userId, ProfileUpdateRequestDto dto) throws Exception {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 닉네임 변경
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }

        // 비밀번호 변경
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 프로필 이미지 업로드
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            String imageUrl = s3Service.uploadFile(dto.getProfileImage(), "profile");
            user.setProfile(imageUrl);
        }

        usersRepository.save(user);

        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .level(user.getLevel())
                .currentExp(user.getPoint())
                .requiredExp(1200)
                .build();
    }
}