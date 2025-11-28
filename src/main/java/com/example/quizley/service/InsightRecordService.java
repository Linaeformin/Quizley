package com.example.quizley.service;

import com.example.quizley.dto.insight.InsightRecordResponseDto;
import com.example.quizley.dto.insight.SameQuestionAnswerRequestDto;
import com.example.quizley.dto.insight.SameQuestionAnswerResponseDto;

import com.example.quizley.entity.balance.BalanceAnswer;
import com.example.quizley.entity.insight.InsightAnswer;
import com.example.quizley.entity.quiz.AiChat;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.comment.Comment;

import com.example.quizley.repository.BalanceAnswerRepository;
import com.example.quizley.repository.QuizRepository;
import com.example.quizley.repository.InsightAnswerRepository;
import com.example.quizley.repository.AiChatRepository;
import com.example.quizley.repository.CommentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsightRecordService {

    private final BalanceAnswerRepository balanceAnswerRepository;
    private final QuizRepository quizRepository;

    private final InsightAnswerRepository insightAnswerRepository;

    private final AiChatRepository aiChatRepository;
    private final CommentRepository commentRepository;

    // 특정 날짜의 인사이트 조회
    public List<InsightRecordResponseDto> getInsightByDate(Long userId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 1) 평일: Comment 기반 출석 (SYSTEM)
        List<Comment> weekdayInsights = commentRepository
                .findInsightsByUserIdAndDateIncludingDeleted(userId, start, end);

        // 2) 주말: BalanceAnswer 기반 출석
        List<BalanceAnswer> weekendInsights =
                balanceAnswerRepository.findByUserIdAndCreatedAtBetween(userId, start, end);

        // 둘 다 합치기
        List<InsightRecordResponseDto> results = new java.util.ArrayList<>();

        // 평일 인사이트 DTO 변환
        for (Comment c : weekdayInsights) {

            if (c.getDeletedAt() != null) {
                results.add(new InsightRecordResponseDto(
                        null,
                        null,
                        date,
                        null,
                        null,
                        "삭제된 인사이트입니다"
                ));
                continue;
            }

            Quiz quiz = c.getQuiz();
            results.add(new InsightRecordResponseDto(
                    quiz.getQuizId(),
                    quiz.getCategory().name(),
                    quiz.getPublishedDate(),
                    quiz.getContent(),
                    aiChatRepository.findByQuiz_QuizIdAndUsers_UserId(quiz.getQuizId(), userId)
                            .map(AiChat::getSummary).orElse(null),
                    c.getFeedback()
            ));
        }


        // 주말 인사이트 DTO 변환
        for (BalanceAnswer b : weekendInsights) {
            Quiz quiz = quizRepository.findById(b.getQuizId())
                    .orElseThrow(() -> new IllegalArgumentException("QUIZ_NOT_FOUND"));

            results.add(new InsightRecordResponseDto(
                    quiz.getQuizId(),
                    quiz.getCategory().name(),
                    quiz.getPublishedDate(),
                    quiz.getContent(),
                    aiChatRepository.findByQuiz_QuizIdAndUsers_UserId(quiz.getQuizId(), userId)
                            .map(AiChat::getSummary).orElse(null),
                    commentRepository.findByQuiz_QuizIdAndUser_UserId(quiz.getQuizId(), userId)
                            .map(Comment::getFeedback).orElse(null)
            ));
        }

        return results;
    }


    // 인사이트 삭제
    @Transactional
    public void deleteInsight(LocalDate date, Long userId) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 삭제 포함해 조회
        List<Comment> comments = commentRepository
                .findInsightsByUserIdAndDateIncludingDeleted(userId, start, end);

        comments.forEach(c -> {
            c.setDeletedAt(LocalDateTime.now()); // Soft delete
        });
    }

    // 같은 질문에 다시 답해보기
    public List<SameQuestionAnswerResponseDto> getSameQuestionAnswers(Long userId, Long quizId) {

        List<InsightAnswer> answers =
                insightAnswerRepository.findByUserIdAndQuizIdOrderByCreatedAtDesc(userId, quizId);

        return answers.stream()
                .map(a -> new SameQuestionAnswerResponseDto(
                        a.getId(),
                        a.getContent(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    // 같은 질문 새 답변 등록
    @Transactional
    public SameQuestionAnswerResponseDto addSameQuestionAnswer(
            Long userId, Long quizId, SameQuestionAnswerRequestDto request
    ) {

        quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("QUIZ_NOT_FOUND"));

        InsightAnswer entity = new InsightAnswer();
        entity.setQuizId(quizId);
        entity.setUserId(userId);
        entity.setContent(request.getAnswer());

        InsightAnswer saved = insightAnswerRepository.save(entity);

        return new SameQuestionAnswerResponseDto(
                saved.getId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

}
