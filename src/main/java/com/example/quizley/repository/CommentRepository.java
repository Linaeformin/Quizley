package com.example.quizley.repository;

import com.example.quizley.dto.community.CommentDto;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 댓글, 응답 레포지토리
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 퀴즈 아이디와 유저 아이디로 응답 여부 확인
    boolean existsByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);

    // 특정 퀴즈의 댓글 목록 조회 (생성 시간 기준 정렬)
    @Query("SELECT new com.example.quizley.dto.community.CommentDto(" +
            "c.commentId, c.content, " +
            "CASE WHEN c.writerAnonymous = true THEN '익명' ELSE u.nickname END, " +
            "c.likeCount, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM CommentLike cl WHERE cl.comment.commentId = c.commentId AND cl.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "c.createdAt) " +
            "FROM Comment c " +
            "LEFT JOIN Users u ON c.user.userId = u.userId " +
            "WHERE c.quiz.quizId = :quizId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<CommentDto> findCommentsByQuizId(@Param("quizId") Long quizId, @Param("userId") Long userId);

    // 최신순
    @Query("SELECT new com.example.quizley.dto.community.CommentDto(" +
            "c.commentId, c.content, " +
            "CASE WHEN c.writerAnonymous = true THEN " +
            "CONCAT('익명', CAST((SELECT COUNT(c2.commentId) FROM Comment c2 " +
            "WHERE c2.quiz.quizId = :quizId AND c2.writerAnonymous = true " +
            "AND c2.createdAt <= c.createdAt AND c2.deletedAt IS NULL) AS string)) " +
            "ELSE u.nickname END, " +
            "c.likeCount, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM CommentLike cl WHERE cl.comment.commentId = c.commentId AND cl.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "c.createdAt) " +
            "FROM Comment c " +
            "LEFT JOIN c.user u " +
            "WHERE c.quiz.quizId = :quizId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<CommentDto> findCommentsByQuizIdOrderByLatest(@Param("quizId") Long quizId, @Param("userId") Long userId);

    // 인기순 쿼리
    @Query("SELECT new com.example.quizley.dto.community.CommentDto(" +
            "c.commentId, c.content, " +
            "CASE WHEN c.writerAnonymous = true THEN " +
            "CONCAT('익명', CAST((SELECT COUNT(c2.commentId) FROM Comment c2 " +
            "WHERE c2.quiz.quizId = :quizId AND c2.writerAnonymous = true " +
            "AND c2.createdAt <= c.createdAt AND c2.deletedAt IS NULL) AS string)) " +
            "ELSE u.nickname END, " +
            "c.likeCount, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM CommentLike cl WHERE cl.comment.commentId = c.commentId AND cl.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "c.createdAt) " +
            "FROM Comment c " +
            "LEFT JOIN c.user u " +
            "WHERE c.quiz.quizId = :quizId " +
            "AND c.deletedAt IS NULL " +
            "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<CommentDto> findCommentsByQuizIdOrderByPopular(@Param("quizId") Long quizId, @Param("userId") Long userId);
}
