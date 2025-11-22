package com.example.quizley.common.level;

import com.example.quizley.dto.users.LevelUpResultDto;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 포인트 적립 및 레벨업 서비스
@Service
@RequiredArgsConstructor
public class LevelService {

    private static final int LEVEL_UP_THRESHOLD = 500;

    private final UsersRepository usersRepository;

    @Transactional
    public LevelUpResultDto tryLevelUp(Long userId, int addedPoint) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        int currentPoint = user.getPoint();
        int newPoint = currentPoint + addedPoint;

        // 레벨업 안 됨
        if (newPoint < LEVEL_UP_THRESHOLD) {
            user.setPoint(newPoint);
            return null;
        }

        // 레벨업 됨
        int newLevel = user.getLevel() + 1;
        int remainingPoint = newPoint - LEVEL_UP_THRESHOLD;

        user.setLevel(newLevel);
        user.setPoint(remainingPoint);

        LevelUpResultDto dto = new LevelUpResultDto(newLevel, remainingPoint);

        // 이번 요청에서 레벨업이 있었다는걸 Context에 기록
        LevelUpContext.set(dto);

        return dto;
    }
}
