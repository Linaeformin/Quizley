package com.example.quizley.repository;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.QuizType;
import com.example.quizley.domain.Origin;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.dto.community.HotQuizDto;
import com.example.quizley.dto.community.QuizListDto;
import com.example.quizley.entity.quiz.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


// 퀴즈 레포지토리
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 이미 존재하는 날짜에 등록된 질문인지
    boolean existsByTypeAndPublishedDateAndCategory(
            QuizType type,
            LocalDate publishedDate,
            Category category
    );

    // 커뮤니티 기능: 오늘의 질문 조회
    Optional<Quiz> findByPublishedDateAndOrigin(
            LocalDate publishedDate,
            Origin origin
    );

    // 특정 날짜의 모든 질문 조회(최신순)
    List<Quiz> findByPublishedDateOrderByCreatedAtDesc(LocalDate publishedDate);

    // 특정 날짜, 특정 카테고리의 질문 조회(최신순, 상위 N개)
    List<Quiz> findByPublishedDateAndCategoryOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Category category,
            Pageable pageable
    );

    // Origin 포함한 조회
    List<Quiz> findByPublishedDateAndOriginAndCategoryOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Origin origin,
            Category category,
            Pageable pageable
    );

    // 특정 날짜, 특정 Origin의 질문 조회 (최신순)
    List<Quiz> findByPublishedDateAndOriginOrderByCreatedAtDesc(
            LocalDate publishedDate,
            Origin origin
    );

    // origin이 SYSTEM이고, 오늘 날짜(published_date)와 카테고리가 일치하는 퀴즈 한 건 조회
    Optional<Quiz> findByOriginAndPublishedDateAndCategory(
            Origin origin,
            LocalDate publishedDate,
            Category category
    );

    boolean existsByQuizIdAndPublishedDate(Long quizId, LocalDate publishedDate);

    Optional<Quiz> findByQuizId(Long quizId);

    // 오늘의 퀴즈 좋아요 여부 확인용
    @Query("SELECT " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM QuizLike l WHERE l.quiz.quizId = :quizId AND l.user.userId = :userId) THEN true " +
            "      ELSE false END) " +
            "FROM Quiz q " +
            "WHERE q.quizId = :quizId")
    Boolean isQuizLikedByUser(@Param("quizId") Long quizId, @Param("userId") Long userId);

    // HOT 게시글 3개 조회 - 차단 필터링 추가
    @Query("SELECT new com.example.quizley.dto.community.HotQuizDto(" +
            "q.quizId, " +
            "q.content, " +
            "q.category, " +
            "CASE WHEN q.isAnonymous = true THEN '익명' ELSE u.nickname END, " +
            "CAST((SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = q.quizId) AS long), " +
            "CAST((SELECT COUNT(c) FROM Comment c WHERE c.quiz.quizId = q.quizId AND c.deletedAt IS NULL) AS long), " +
            "q.createdAt, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM QuizLike l2 WHERE l2.quiz.quizId = q.quizId AND l2.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            // 본인 여부
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN q.userId = :userId THEN true " +
            "      ELSE false END)) " +
            "FROM Quiz q " +
            "LEFT JOIN Users u ON q.userId = u.userId " +
            "WHERE q.publishedDate = :date " +
            "AND q.origin = :origin " +
            "AND q.category = :category " +
            // 차단한 사용자의 게시물 제외
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = :userId AND b.blockedId = q.userId" +
            ")) " +
            // 나를 차단한 사용자의 게시물 제외
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = q.userId AND b.blockedId = :userId" +
            ")) " +
            "ORDER BY (SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = q.quizId) DESC")
    List<HotQuizDto> findTop3ByDateAndCategoryOrderByLikes(
            @Param("date") LocalDate date,
            @Param("origin") Origin origin,
            @Param("category") Category category,
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 게시글 목록 조회(카테고리 필터링)
    @Query("SELECT new com.example.quizley.dto.community.QuizListDto(" +
            "q.quizId, " +
            "q.content, " +
            "q.category, " +
            "CASE WHEN q.isAnonymous = true THEN '익명' ELSE u.nickname END, " +
            "CAST((SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = q.quizId) AS long), " +
            "CAST((SELECT COUNT(c) FROM Comment c WHERE c.quiz.quizId = q.quizId AND c.deletedAt IS NULL) AS long), " +
            "q.createdAt, " +
            "q.publishedDate, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM QuizLike l2 WHERE l2.quiz.quizId = q.quizId AND l2.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "(CASE WHEN :userId IS NULL THEN FALSE " +
            "      WHEN q.userId = :userId THEN TRUE " +
            "      ELSE FALSE END), " +
            "q.userId) " +
            "FROM Quiz q " +
            "LEFT JOIN Users u ON q.userId = u.userId " +
            "WHERE q.publishedDate = :date " +
            "AND q.origin = :origin " +
            "AND q.category = :category " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = :userId AND b.blockedId = q.userId" +
            ")) " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = q.userId AND b.blockedId = :userId" +
            ")) " +
            "ORDER BY q.createdAt DESC")
    List<QuizListDto> findQuizListByCategory(
            @Param("date") LocalDate date,
            @Param("origin") Origin origin,
            @Param("category") Category category,
            @Param("userId") Long userId
    );

    // 게시글 목록 조회(전체)
    @Query("SELECT new com.example.quizley.dto.community.QuizListDto(" +
            "q.quizId, " +
            "q.content, " +
            "q.category, " +
            "CASE WHEN q.isAnonymous = true THEN '익명' ELSE u.nickname END, " +
            "CAST((SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = q.quizId) AS long), " +
            "CAST((SELECT COUNT(c) FROM Comment c WHERE c.quiz.quizId = q.quizId AND c.deletedAt IS NULL) AS long), " +
            "q.createdAt, " +
            "q.publishedDate, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM QuizLike l2 WHERE l2.quiz.quizId = q.quizId AND l2.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "(CASE WHEN :userId IS NULL THEN FALSE " +
            "      WHEN q.userId = :userId THEN TRUE " +
            "      ELSE FALSE END), " +
            "q.userId) " +
            "FROM Quiz q " +
            "LEFT JOIN Users u ON q.userId = u.userId " +
            "WHERE q.publishedDate = :date " +
            "AND q.origin = :origin " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = :userId AND b.blockedId = q.userId" +
            ")) " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = q.userId AND b.blockedId = :userId" +
            ")) " +
            "ORDER BY q.createdAt DESC")
    List<QuizListDto> findAllQuizList(
            @Param("date") LocalDate date,
            @Param("origin") Origin origin,
            @Param("userId") Long userId
    );

    // 퀴즈 좋아요 개수 조회
    @Query("SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = :quizId")
    Long countLikesByQuizId(@Param("quizId") Long quizId);

    // 댓글 개수 조회
    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.quiz.quizId = :quizId " +
            "AND c.deletedAt IS NULL " +
            "AND c.commentAnonymous = com.example.quizley.domain.CommentAnonymous.OPEN")
    Long countCommentsByQuizId(@Param("quizId") Long quizId);

    // 키워드로 퀴즈 검색 (최신순) - 차단 필터링 추가
    @Query("SELECT new com.example.quizley.dto.community.QuizListDto(" +
            "q.quizId, " +
            "q.content, " +
            "q.category, " +
            "CASE WHEN q.isAnonymous = true THEN '익명' ELSE u.nickname END, " +
            "CAST((SELECT COUNT(l) FROM QuizLike l WHERE l.quiz.quizId = q.quizId) AS long), " +
            "CAST((SELECT COUNT(c) FROM Comment c WHERE c.quiz.quizId = q.quizId AND c.deletedAt IS NULL) AS long), " +
            "q.createdAt, " +
            "q.publishedDate, " +
            "(CASE WHEN :userId IS NULL THEN false " +
            "      WHEN EXISTS (SELECT 1 FROM QuizLike l2 WHERE l2.quiz.quizId = q.quizId AND l2.user.userId = :userId) THEN true " +
            "      ELSE false END), " +
            "(CASE WHEN :userId IS NULL THEN FALSE " +
            "      WHEN q.userId = :userId THEN TRUE " +
            "      ELSE FALSE END), " +
            "q.userId) " +
            "FROM Quiz q " +
            "LEFT JOIN Users u ON q.userId = u.userId " +
            "WHERE q.content LIKE %:keyword% " +
            "AND q.origin = com.example.quizley.domain.Origin.USER " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = :userId AND b.blockedId = q.userId" +
            ")) " +
            "AND (:userId IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM BlockUser b " +
            "    WHERE b.blockerId = q.userId AND b.blockedId = :userId" +
            ")) " +
            "ORDER BY q.createdAt DESC")
    List<QuizListDto> searchQuizzesByKeywordOrderByLatest(
            @Param("keyword") String keyword,
            @Param("userId") Long userId
    );

    // 키워드로 퀴즈 검색 결과 개수 조회
    @Query("SELECT COUNT(q) FROM Quiz q " +
            "WHERE q.content LIKE %:keyword% " +
            "AND q.origin = com.example.quizley.domain.Origin.USER")
    Long countByContentContaining(@Param("keyword") String keyword);

    // 내가 작성한 게시글 조회
    List<Quiz> findByUserIdAndOriginOrderByCreatedAtDesc(Long userId, Origin origin);

    // 주말용 (카테고리 X)
    Optional<Quiz> findFirstByOriginAndTypeAndPublishedDate(
            Origin origin,
            QuizType type,
            LocalDate publishedDate
    );

    @Query("SELECT q FROM Quiz q " +
            "WHERE q.origin = :origin " +
            "AND q.type = :type " +
            "AND q.publishedDate = :date " +
            "AND q.category = :category")
    Optional<Quiz> findByOriginAndTypeAndPublishedDateAndCategory(
            @Param("origin") Origin origin,
            @Param("type") QuizType type,
            @Param("date") LocalDate date,
            @Param("category") Category category
    );
}