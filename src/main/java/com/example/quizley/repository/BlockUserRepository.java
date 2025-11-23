package com.example.quizley.repository;

import com.example.quizley.entity.users.BlockUser;
import com.example.quizley.entity.users.BlockUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockUserRepository extends JpaRepository<BlockUser, BlockUserId> {

    // 특정 사용자가 다른 사용자를 차단했는지 확인
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    // 차단 관계 조회
    Optional<BlockUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    // 특정 사용자가 차단한 모든 사용자 목록
    @Query("SELECT b.blockedId FROM BlockUser b WHERE b.blockerId = :blockerId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
}