package com.example.quizley.service;

import com.example.quizley.dto.report.ReportResponseDto;
import com.example.quizley.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final int MIN_COHORT = 30;
    private final CalendarService calendarService;
    private final ReportRepository reportRepository;

    // 모든 카테고리 리스트 (한글 라벨 그대로)
    private static final List<String> ALL_CATEGORIES = List.of(
            "심리학", "자연과학", "역사", "예술", "문학", "미스터리"
    );

    // 카테고리 한글 라벨 매핑
    private static final Map<String, String> CATEGORY_LABELS = Map.of(
            "PSYCHOLOGY", "심리학",
            "SCIENCE", "자연과학",
            "HISTORY", "역사",
            "ART", "예술",
            "LITERATURE", "문학",
            "MYSTERY", "미스터리"
    );

    // 랜덤 메시지 세트
    private static final List<String> STRONG_MESSAGES = List.of(
            "%s 분야에서 정말 뛰어난 참여를 보여주고 있어요! 🔥",
            "이번 달은 %s 분야 전문가 같아요! 멋져요 😊",
            "%s 분야 문제에 특히 강한 모습을 보여주었어요!",
            "꾸준함이 돋보여요! %s 분야에서 좋은 패턴이 보입니다."
    );

    private static final List<String> WEAK_MESSAGES = List.of(
            "이번에는 %s 분야 문제도 도전해보지 않을래요? 😊",
            "%s 분야를 풀면 더 균형 잡힌 실력을 만들 수 있을 거예요!",
            "새로운 분야 %s 문제도 재미있게 풀어볼 수 있을 거예요!",
            "%s 분야 문제도 시도해보면 더 성장할 수 있어요!"
    );

    public ReportResponseDto generateReport(Long userId) {

        // streak 계산
        var calendar = calendarService.getCalendar(userId);
        int streakDays = calendar.getConsecutiveDays();

        // 현재 카테고리별 푼 횟수 가져오기 (영문 코드 기반)
        Map<String, Integer> rawScores = reportRepository.countByCategoryForUser(userId);

        // 한글 라벨 매핑
        Map<String, Integer> labeledScores = convertLabels(rawScores);

        // 모든 카테고리 0 포함하여 채우기
        Map<String, Integer> completedScores = fillMissingCategories(labeledScores);

        // 0~100 정규화
        Map<String, Integer> normalizedScores = normalizeScores(completedScores);

        // 대표 카테고리 계산
        String dominant = findDominant(completedScores);
        String least = findLeast(completedScores);

        // 상위 % 계산
        double topPercent = computeTopPercentByCurrentStreak(userId, streakDays);

        // 피드백 문구 생성
        String feedback = generateFeedback(dominant, least);

        return ReportResponseDto.builder()
                .streakDays(streakDays)
                .topPercent(topPercent)
                .dominantCategory(dominant)
                .scores(normalizedScores)
                .feedback(feedback)
                .build();
    }

    // 한글 라벨 변환
    private Map<String, Integer> convertLabels(Map<String, Integer> raw) {
        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> CATEGORY_LABELS.getOrDefault(e.getKey(), e.getKey()),
                        Map.Entry::getValue
                ));
    }

    // 모든 카테고리 0 포함해서 채우기
    private Map<String, Integer> fillMissingCategories(Map<String, Integer> labeledScores) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String cat : ALL_CATEGORIES) {
            result.put(cat, labeledScores.getOrDefault(cat, 0));
        }
        return result;
    }

    // 0~100 정규화
    private Map<String, Integer> normalizeScores(Map<String, Integer> scores) {
        int max = scores.values().stream().max(Integer::compareTo).orElse(1);

        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (int) Math.round((e.getValue() * 100.0) / max)
                ));
    }

    private String findDominant(Map<String, Integer> stats) {
        return stats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("기타");
    }

    private String findLeast(Map<String, Integer> stats) {
        return stats.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");
    }

    // 상위 퍼센트 계산
    private double computeTopPercentByCurrentStreak(Long userId, int myCurrentStreak) {
        var rows = reportRepository.findCurrentStreakAllUsers();
        if (rows == null || rows.size() < MIN_COHORT) {
            return estimateTopPercent(myCurrentStreak);
        }

        Map<Long, Integer> map = new HashMap<>();
        for (Object[] r : rows) {
            Long uid = ((Number) r[0]).longValue();
            Integer len = ((Number) r[1]).intValue();
            map.put(uid, len);
        }

        int my = map.getOrDefault(userId, 0);

        long total = map.size();
        long above = map.values().stream().filter(v -> v > my).count();
        long equal = map.values().stream().filter(v -> v == my).count();

        double top = 100.0 * (above + 0.5 * equal) / total;

        top = Math.round(top);
        return Math.max(1.0, Math.min(99.0, top));
    }

    private double estimateTopPercent(int streak) {
        if (streak >= 10) return 10.0;
        if (streak >= 7) return 30.0;
        if (streak >= 5) return 50.0;
        return 70.0;
    }

    // 피드백 생성
    private String generateFeedback(String dominant, String least) {
        String strong = random(STRONG_MESSAGES).formatted(dominant);

        // least가 dominant와 같으면 분야가 편중된 경우 메시지 변경
        if (least.equals(dominant)) {
            return strong + " 다양한 분야 문제도 도전해보면 더 좋아요! 😊";
        }

        String weak = random(WEAK_MESSAGES).formatted(least);
        return strong + " " + weak;
    }

    // 리스트에서 랜덤 메시지 하나 return
    private String random(List<String> list) {
        return list.get(new Random().nextInt(list.size()));
    }
}
