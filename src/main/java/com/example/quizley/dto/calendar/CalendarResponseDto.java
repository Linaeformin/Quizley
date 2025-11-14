package com.example.quizley.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    private Long userId;
    private int consecutiveDays; // 연속 응답일
    private List<LocalDate> answeredDates; // 응답한 날짜 목록
}