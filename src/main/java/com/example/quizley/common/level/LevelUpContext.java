package com.example.quizley.common.level;

import com.example.quizley.dto.users.LevelUpResultDto;


// 레벨업 결과를 임시로 저장
public class LevelUpContext {

    private static final ThreadLocal<LevelUpResultDto> holder = new ThreadLocal<>();

    public static void set(LevelUpResultDto dto) {
        holder.set(dto);
    }

    public static LevelUpResultDto getAndClear() {
        LevelUpResultDto dto = holder.get();
        holder.remove();
        return dto;
    }

    public static void clear() {
        holder.remove();
    }
}
