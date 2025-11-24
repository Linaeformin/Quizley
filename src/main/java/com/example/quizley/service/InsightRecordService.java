package com.example.quizley.service;

import com.example.quizley.dto.insight.InsightRecordResponseDto;
import com.example.quizley.dto.insight.SameQuestionAnswerRequestDto;
import com.example.quizley.dto.insight.SameQuestionAnswerResponseDto;
import com.example.quizley.entity.balance.BalanceAnswer;
import com.example.quizley.entity.insight.InsightAnswer;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.quiz.AiChat;
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

    // 🔽 요약/피드백 읽어오기 위해 추가
    private final AiChatRepository aiChatRepository;
    private final CommentRepository commentRepository;

    // ✅ 특정 날짜에 사용자가 실제로 푼 인사이트 조회
    public List<InsightRecordResponseDto> getInsightByDate(Long userId, LocalDate date) {

        // 해당 날짜 00:00~23:59 범위 생성
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 사용자가 해당 날짜에 푼 퀴즈 목록 조회 (주말 밸런스 기준)
        List<BalanceAnswer> answers =
                balanceAnswerRepository.findByUserIdAndCreatedAtBetween(userId, start, end);

        if (answers.isEmpty()) {
            throw new IllegalArgumentException(date + " 날짜에 사용자가 푼 인사이트가 없습니다.");
        }

        // 각 answer마다 quiz 정보 조회 + DTO 변환
        return answers.stream()
                .map(answer -> {
                    Quiz quiz = quizRepository.findById(answer.getQuizId())
                            .orElseThrow(() -> new IllegalArgumentException("퀴즈 정보를 찾을 수 없습니다."));

                    Long quizId = quiz.getQuizId();

                    // 🔹 1) AiChat.summary에서 요약 읽기 (있으면)
                    String summary = aiChatRepository
                            .findByQuiz_QuizIdAndUsers_UserId(quizId, userId)
                            .map(AiChat::getSummary)
                            .orElse(null);

                    // 🔹 2) Comment.feedback에서 피드백 읽기 (있으면)
                    String feedback = commentRepository
                            .findByQuiz_QuizIdAndUser_UserId(quizId, userId)
                            .map(Comment::getFeedback)
                            .orElse(null);

                    // 🔹 3) DTO에 summary / feedback 함께 담아 반환
                    return new InsightRecordResponseDto(
                            quiz.getQuizId(),
                            quiz.getCategory().name(),
                            quiz.getPublishedDate(),
                            quiz.getContent(),
                            summary,   // summary
                            feedback   // feedback
                    );
                })
                .toList();
    }

    // ✅ 사용자가 푼 특정 인사이트 삭제
    @Transactional
    public void deleteInsight(Long quizId, Long userId) {
        // 사용자가 푼 정답만 삭제 (퀴즈 자체는 삭제 X)
        balanceAnswerRepository.deleteByQuizIdAndUserId(quizId, userId);
    }

    // ✅ 같은 질문 과거 답변 목록 조회
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

    // ✅ 같은 질문에 새 답변 추가
    @Transactional
    public SameQuestionAnswerResponseDto addSameQuestionAnswer(
            Long userId,
            Long quizId,
            SameQuestionAnswerRequestDto request
    ) {
        quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈를 찾을 수 없습니다."));

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
