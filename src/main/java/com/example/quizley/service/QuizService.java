package com.example.quizley.service;

import com.example.quizley.domain.*;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository repo;
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    // 퀴즈 생성 및 일주일 뒤 공개 설정
    @Transactional
    public Long saveSystemWeekday(String categoryKo, String content){
        // 질문 생성일 및 질문 공개일 설정
        var now = LocalDateTime.now(ZONE);
        var published = now.toLocalDate().plusDays(7);
        Category category = Category.valueOf(categoryKo);

        // 중복 생성 방지
        if (repo.existsByTypeAndPublishedDateAndCategory(QuizType.WEEKDAY, published, category)) {
            return null; // 혹은 기존 엔티티 ID 반환 로직/스킵 로그 등
        }

        // 엔티티 조립
        var quiz = Quiz.builder()
                .origin(Origin.SYSTEM)
                .type(QuizType.WEEKDAY)
                .category(Category.valueOf(categoryKo))
                .content(content)
                .createdAt(now)
                .modifiedAt(now)
                .publishedDate(published)
                .build();

        // 질문 저장
        return repo.save(quiz).getQuizId();
    }

    // 카테고리 한 번에 저장
    @Transactional
    public int saveSystemWeekdayBulk(Map<String,String> map){
        // 실제 저장 건수 카운트
        int count = 0;

        for (var e : map.entrySet()) {
            // 영문 키 → 한글 카테고리명 매핑
            String ko = keyToKo(e.getKey());
            if (ko == null || e.getValue() == null || e.getValue().isBlank()) continue;

            // 단건 저장 호출(중복이면 null 반환)
            Long id = saveSystemWeekday(ko, e.getValue().trim());

            // 저장에 성공했을 때만 카운트 증가시키는 게 정확함
            if (id != null) count++;
        }
        return count;
    }

    // 영문 카테고리 키를 한글로 변환
    private String keyToKo(String k){
        return switch (k) {
            case "mystery" -> "미스터리";
            case "science" -> "과학";
            case "liter"   -> "문학";
            case "art"     -> "예술";
            case "history" -> "역사";
            case "mind"    -> "심리";
            default -> null;
        };
    }
}

