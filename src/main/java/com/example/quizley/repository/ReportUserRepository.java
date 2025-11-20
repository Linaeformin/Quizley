package com.example.quizley.repository;

import com.example.quizley.domain.ContentType;
import com.example.quizley.entity.users.ReportUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportUserRepository extends JpaRepository<ReportUser, Long> {

    // 특정 사용자가 특정 콘텐츠를 이미 신고했는지 확인
    boolean existsByReporterIdAndContentTypeAndContentId(
            Long reporterId,
            ContentType contentType,
            Long contentId
    );

    // 특정 콘텐츠에 대한 신고 조회
    Optional<ReportUser> findByReporterIdAndContentTypeAndContentId(
            Long reporterId,
            ContentType contentType,
            Long contentId
    );
}
