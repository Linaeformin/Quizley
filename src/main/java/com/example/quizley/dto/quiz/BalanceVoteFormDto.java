package com.example.quizley.dto.quiz;

import com.example.quizley.domain.BalanceSide;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


// 투표 요청 DTO
@Getter @Setter
@Builder
public class BalanceVoteFormDto {
    private Long quizId;
    private BalanceSide side;
}
