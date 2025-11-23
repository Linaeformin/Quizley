package com.example.quizley.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatUtil {
    public static String formatTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        Long hours = duration.toHours();

        if (minutes < 1) {
            return "방금 전";
        } else if(minutes <60){
            return minutes + "분 전";
        } else if(hours < 24){
            return hours + "시간 전";
        } else {
            //24시간 이상이면 날짜만 표시
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return createdAt.format(formatter);
        }
    }
}
