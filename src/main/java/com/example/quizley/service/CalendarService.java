package com.example.quizley.service;

import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDto getCalendar(Long userId) {

        // 문자열로 날짜를 가져오기 (형식: "2025-11-13")
        List<String> answeredDateStrings = calendarRepository.findAnsweredDateStringsByUserId(userId);

        // 응답 기록이 없을 경우
        if (answeredDateStrings.isEmpty()) {
            return new CalendarResponseDto(userId, 0, Collections.emptyList());
        }

        // LocalDate로 변환하여 정렬
        List<LocalDate> answeredDates = answeredDateStrings.stream()
                .map(LocalDate::parse)
                .sorted()
                .toList();

        // 연속 응답일 계산
        int consecutiveDays = calculateConsecutiveDays(answeredDates);

        // DTO 반환
        return new CalendarResponseDto(userId, consecutiveDays, answeredDates);
    }

    private int calculateConsecutiveDays(List<LocalDate> dates) {
        int streak = 1;
        int maxStreak = 1;

        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).minusDays(1).equals(dates.get(i - 1))) {
                streak++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 1;
            }
        }
        return maxStreak;
    }
}
