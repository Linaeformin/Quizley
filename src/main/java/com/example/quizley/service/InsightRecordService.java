package com.example.quizley.service;

import com.example.quizley.domain.Origin;
import com.example.quizley.dto.insight.InsightRecordResponseDto;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.repository.BalanceAnswerRepository;
import com.example.quizley.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InsightRecordService {

    private final QuizRepository quizRepository;
    private final BalanceAnswerRepository balanceAnswerRepository;

    // 선택한 날짜의 인사이트 조회
    public InsightRecordResponseDto getInsightByDate(LocalDate date) {
        Quiz quiz = quizRepository.findByPublishedDateAndOrigin(date, Origin.SYSTEM)
                .orElseThrow(() -> new IllegalArgumentException(date + " 날짜의 인사이트(퀴즈)를 찾을 수 없습니다."));

        return new InsightRecordResponseDto(
                quiz.getQuizId(),
                quiz.getCategory().name(),
                quiz.getPublishedDate(),
                quiz.getContent(),
                null, // 요약 (팀원 기능 연동 시 추가)
                null  // 피드백 (팀원 기능 연동 시 추가)
        );
    }

    // 선택한 날짜의 인사이트 삭제 (응답 포함)
    @Transactional
    public void deleteInsight(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("해당 인사이트(퀴즈)를 찾을 수 없습니다."));

        balanceAnswerRepository.deleteByQuizIdAndUserId(quizId, userId);

        quizRepository.delete(quiz);
    }
}
