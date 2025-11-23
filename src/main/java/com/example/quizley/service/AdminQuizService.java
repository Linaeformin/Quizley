package com.example.quizley.service;

import com.example.quizley.config.claude.ClaudeClient;
import com.example.quizley.config.claude.PromptLoader;
import com.example.quizley.domain.BalanceSide;
import com.example.quizley.domain.Origin;
import com.example.quizley.domain.QuizType;
import com.example.quizley.dto.quiz.WeekendQuizCreatedFormDto;
import com.example.quizley.entity.balance.QuizBalance;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.repository.QuizBalanceRepository;
import com.example.quizley.repository.QuizRepository;
import com.example.quizley.storage.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import static com.example.quizley.config.claude.WeekdayPromptType.QUIZ;


// 오늘의 퀴즈 생성 서비스
@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final PromptLoader promptLoader;
    private final ClaudeClient claudeClient;
    private final QuizService quizService;
    private final QuizBalanceRepository quizBalanceRepository;
    private final QuizRepository quizRepository;
    private final S3Service s3Service;

    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.anthropic.model}")
    private String model;

    @Value("${app.anthropic.max-tokens}")
    private Integer maxTokens;

    // 평일 퀴즈 생성
    @Transactional
    public int generateWeekdayQuizFromAi() {
        // 1) 프롬프트 로드
        String prompt = promptLoader.load(QUIZ);

        // 2) LLM 호출
        String joined = claudeClient.call(model, maxTokens.longValue(), 0.0, prompt);
        if (joined == null || joined.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "NO_ANTHROPIC");
        }

        // 3) 코드펜스 제거 + JSON 오브젝트 추출
        String cleaned = stripFence(joined);
        String json = extractJsonObject(cleaned);

        // 4) Map<String,String> 파싱
        Map<String,String> map = parseJsonMap(json);
        if (map.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CONSTRAINT_VIOLATION");
        }

        // 5) 기존 QuizService의 bulk 메서드 재사용
        return quizService.saveSystemWeekdayBulk(map);
    }

    // JSON으로 응답 파싱
    private Map<String,String> parseJsonMap(String json) {
        try {
            return om.readValue(json, new TypeReference<Map<String,String>>() {});
        } catch (JsonProcessingException e) {
            String cut = json == null ? "null" :
                    (json.length() > 300 ? json.substring(0,300) + "...(truncated)" : json);
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage() + " / 원문=" + cut, e);
        }
    }

    // 마크다운 삭제
    private static String stripFence(String s){
        if (s == null) return "";
        String t = s.trim();

        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl > 0) t = t.substring(nl + 1);
            int end = t.lastIndexOf("```");
            if (end >= 0) t = t.substring(0, end);
            t = t.trim();
        }
        return t;
    }

    // 중괄호 꺼내기
    private static String extractJsonObject(String s){
        if (s == null) return "";
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        return (start >= 0 && end > start) ? s.substring(start, end + 1).trim() : s.trim();
    }

    // 밸런스 게임 생성
    @Transactional
    public void createBalanceGame(
            WeekendQuizCreatedFormDto request,
            MultipartFile optionAImage,
            MultipartFile optionBImage
    ) throws IOException {

        // 1) Quiz 생성 (category는 밸런스일 때 null 허용)
        Quiz quiz = new Quiz();
        quiz.setOrigin(Origin.SYSTEM);
        quiz.setType(QuizType.WEEKEND);
        quiz.setContent(request.getContent());
        quiz.setCategory(null); // 밸런스는 카테고리 없음
        quiz.setUserId(null);
        quiz.setIsAnonymous(null);
        quiz.setPublishedDate(request.getPublishedDate());

        Quiz savedQuiz = quizRepository.save(quiz);

        // 2) 이미지 업로드
        String optionAUrl = uploadIfNotEmpty(optionAImage);
        String optionBUrl = uploadIfNotEmpty(optionBImage);

        // 3) 선택지 생성
        QuizBalance balanceA = QuizBalance.of(
                savedQuiz.getQuizId(),   // 또는 getId()
                BalanceSide.A,
                request.getOptionALabel(),
                optionAUrl
        );

        QuizBalance balanceB = QuizBalance.of(
                savedQuiz.getQuizId(),
                BalanceSide.B,
                request.getOptionBLabel(),
                optionBUrl
        );

        quizBalanceRepository.saveAll(List.of(balanceA, balanceB));
    }

    // S3에 저장
    private String uploadIfNotEmpty(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CONSTRAINT_VIOLATION");
        }
        // balance 폴더에 저장
        return s3Service.uploadFile(file, "balance");
    }
}
