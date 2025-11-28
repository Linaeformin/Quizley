package com.example.quizley.repository;

import com.example.quizley.domain.Origin;
import com.example.quizley.domain.Status;
import com.example.quizley.dto.community.CommentDto;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 댓글, 응답 레포지토리
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 퀴즈 아이디와 유저 아이디로 응답 여부 확인
    boolean existsByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);

    // 특정 퀴즈의 댓글 목록 조회 (생성 시간 기준 정렬)
    @Query("SELECT new com.example.quizley.dto.community.CommentDto(" +
            "c.commentId, c.user.userId, c.content, " +
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
            "c.commentId, c.user.userId, c.content, " +
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
            "c.commentId, c.user.userId, c.content, " +
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

    // 퀴즈 id과 유저 id로 의견 찾기
    Optional<Comment> findByQuiz_QuizIdAndUser_UserId(Long quizId, Long userId);

    // 인사이트용: DONE + OPEN + 내 댓글 제외 + 인기순
    @Query("SELECT new com.example.quizley.dto.community.CommentDto(" +
            "c.commentId, c.user.userId, c.content, " +
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
            "AND c.status = com.example.quizley.domain.Status.DONE " +
            "AND c.commentAnonymous = com.example.quizley.domain.CommentAnonymous.OPEN " +
            "AND c.user.userId <> :userId " +
            "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<CommentDto> findInsightCommentsByQuizIdExceptUser(
            @Param("quizId") Long quizId,
            @Param("userId") Long userId
    );

    // 삭제되지 않은 댓글을 찾는 메소드
    Optional<Comment> findByQuiz_QuizIdAndUser_UserIdAndDeletedAtIsNull(Long quizId, Long userId);

    // 변경: 오늘의 SYSTEM 퀴즈 중, 이 유저가 DONE으로 하나라도 답했는지
    boolean existsByUser_UserIdAndQuiz_OriginAndQuiz_PublishedDateAndStatus(
            Long userId,
            Origin origin,
            LocalDate publishedDate,
            Status status
    );

    // user + quiz 기준, 가장 최근 PROGRESS 댓글
    Optional<Comment> findFirstByUser_UserIdAndQuiz_QuizIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId,
            Long quizId,
            Status status
    );

    // 내가 작성한 댓글 최신순으로 조회
    List<Comment> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // 스토리 알림에서 사용
    @Query("SELECT c FROM Comment c WHERE DATE(c.createdAt) = :date")
    List<Comment> findByCreatedAtDate(LocalDate date);

    // 1년전 답변한 게시글 알림
    @Query("""
    SELECT c FROM Comment c
    WHERE c.quiz.origin = com.example.quizley.domain.Origin.SYSTEM
      AND c.quiz.publishedDate = :date
      AND c.status = com.example.quizley.domain.Status.DONE
      AND c.deletedAt IS NULL""")
    List<Comment> findSystemAnswersByQuizDate(@Param("date") LocalDate date);

    // 평일 출석
    @Query("""
    SELECT c FROM Comment c
    WHERE c.user.userId = :userId
        AND c.status = com.example.quizley.domain.Status.DONE
        AND c.quiz.publishedDate = :date
        AND c.deletedAt IS NULL""")
    List<Comment> findDoneInsightsByUserIdAndDate(Long userId, LocalDate date);

    // 인사이트 삭제
    @Query("""
SELECT c FROM Comment c
WHERE c.user.userId = :userId
  AND c.createdAt BETWEEN :start AND :end
""")
    List<Comment> findInsightsByUserIdAndDateIncludingDeleted(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
