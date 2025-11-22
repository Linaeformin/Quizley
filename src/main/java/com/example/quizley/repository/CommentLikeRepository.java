package com.example.quizley.repository;
import com.example.quizley.entity.comment.CommentLike;
import com.example.quizley.entity.comment.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId>{
    // 특정 사용자가 특정 댓글에 좋아요를 눌렀는지 확인
    boolean existsByComment_CommentIdAndUser_UserId(Long commentId, Long userId);
    // 특정 사용자의 특정 댓글 좋아요 찾기
    Optional<CommentLike> findByComment_CommentIdAndUser_UserId(Long commentId, Long userId);
}
