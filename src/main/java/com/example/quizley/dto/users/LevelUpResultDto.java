package com.example.quizley.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;


// 레벨업 시 반환하는 DTO
@Getter
@AllArgsConstructor
public class LevelUpResultDto {

    // 레벨업 후 현재 레벨
    private final int currentLevel;

    // 레벨업 후 남은 포인트 (요구사항상 0으로 초기화)
    private final int remainingPoint;
}
