package com.example.quizley.repository;
import com.example.quizley.entity.quiz.QuizLike;
import com.example.quizley.entity.quiz.QuizLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface QuizLikeRepository extends JpaRepository<QuizLike, QuizLikeId> {
    // 특정 사용자가 특정 퀴즈에 좋아요 눌렀는지 확인
    boolean existsByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);

    // 특정 사용자의 특정 퀴즈 좋아요 찾기
    Optional<QuizLike> findByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);

    // 내가 좋아요 누른 게시물 찾기
    List<QuizLike> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
