package com.example.quizley.repository;

import com.example.quizley.entity.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

// 댓글, 응답 레포지토리
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 퀴즈 아이디와 유저 아이디로 응답 여부 확인
    boolean existsByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);
}
