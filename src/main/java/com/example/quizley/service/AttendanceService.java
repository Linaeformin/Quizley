package com.example.quizley.service;

import com.example.quizley.common.level.LevelService;
import com.example.quizley.dto.users.LevelUpResultDto;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;


// 출석 서비스
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final int ATTENDANCE_POINT = 10;

    private final UsersRepository usersRepository;
    private final LevelService levelService;

    @Transactional
    public LevelUpResultDto attendToday(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_ATTENDED"));

        LocalDate today = LocalDate.now();

        // 이미 오늘 출석한 경우 → 포인트/레벨업 X
        if (today.equals(user.getLastAttendanceDate())) {
            return null;
        }

        // 오늘 첫 출석 → 출석일 갱신
        user.setLastAttendanceDate(today);

        // 포인트 적립 + 레벨업 시도
        return levelService.tryLevelUp(userId, ATTENDANCE_POINT);
    }
}
