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

    // Repository를 주입받아서 DB 접근
    private final CalendarRepository calendarRepository;

    public CalendarResponseDto getCalendar(Long userId) {

        // DB에서 유저가 응답한 날짜 목록 조회
        List<LocalDate> answeredDates = calendarRepository.findAnsweredDatesByUserId(userId);

        // 아무 응답 기록이 없는 경우 → 연속일 0으로 반환
        if (answeredDates.isEmpty()) {
            return new CalendarResponseDto(userId, 0, Collections.emptyList());
        }

        // 날짜 오름차순 정렬 (연속 계산을 위해)
        answeredDates.sort(LocalDate::compareTo);

        // 연속 응답일 계산
        int consecutiveDays = calculateConsecutiveDays(answeredDates);

        // 결과를 DTO로 만들어 Controller에 반환
        return new CalendarResponseDto(userId, consecutiveDays, answeredDates);
    }

    // 연속 응답일 계산 로직
    private int calculateConsecutiveDays(List<LocalDate> dates) {
        int streak = 1;
        int maxStreak = 1;

        for (int i = 1; i < dates.size(); i++) {

            // 오늘 날짜가 어제(dates[i-1]) + 1일인 경우 → 연속
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