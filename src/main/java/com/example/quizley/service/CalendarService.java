package com.example.quizley.service;

import com.example.quizley.dto.calendar.CalendarResponseDto;
import com.example.quizley.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDto getCalendar(Long userId) {

        List<Object> aiChatDates = calendarRepository.findAiChatDates(userId);
        List<Object> balanceDates = calendarRepository.findBalanceDates(userId);

        List<LocalDate> answeredDates = Stream.concat(
                        aiChatDates.stream(),
                        balanceDates.stream()
                )
                .map(obj -> {
                    if (obj instanceof java.sql.Date sqlDate) {
                        return sqlDate.toLocalDate();
                    }
                    if (obj instanceof LocalDate ld) {
                        return ld;
                    }
                    throw new IllegalStateException("Unknown date type: " + obj.getClass());
                })
                .distinct()
                .sorted()
                .toList();

        int consecutiveDays = calculateConsecutiveDays(answeredDates);

        return new CalendarResponseDto(userId, consecutiveDays, answeredDates);
    }


    private int calculateConsecutiveDays(List<LocalDate> dates) {

        if (dates.isEmpty()) return 0;

        int streak = 1;
        int maxStreak = 1;

        for (int i = 1; i < dates.size(); i++) {

            if (dates.get(i - 1).plusDays(1).equals(dates.get(i))) {
                streak++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 1;
            }
        }
        return maxStreak;
    }
}
