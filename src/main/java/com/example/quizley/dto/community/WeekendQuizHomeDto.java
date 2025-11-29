package com.example.quizley.dto.community;

import com.example.quizley.dto.community.WeekendQuizVoteResultDto;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class WeekendQuizHomeDto {
    private Long quizId;
    private String content;
    private String category; // 주말 퀴즈는 카테고리가 의미 없을 수 있지만, 기존 구조 유지를 위해 포함
    private LocalDate publishedDate;
    private Boolean isLiked; // 좋아요 기능이 주말 퀴즈에도 있다면 포함
    private String quizType = "WEEKEND"; // 주말 퀴즈임을 명시

    private WeekendQuizVoteResultDto voteResult; // 투표 결과 정보
}