package com.example.quizley.service;

import com.example.quizley.domain.*;
import com.example.quizley.dto.quiz.ChatRoomFormDto;
import com.example.quizley.dto.quiz.WeekdayQuizResDto;
import com.example.quizley.entity.quiz.AiChat;
import com.example.quizley.entity.quiz.AiMessage;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;


// 퀴즈 서비스
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final AiChatRepository aiChatRepository;
    private final AiMessageRepository aiMessageRepository;
    private final CommentRepository commentRepository;
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private final UsersRepository usersRepository;

    // 퀴즈 생성 및 일주일 뒤 공개 설정
    @Transactional
    public Long saveSystemWeekday(String categoryKo, String content){
        // 질문 생성일 및 질문 공개일 설정
        var now = LocalDateTime.now(ZONE);
        var published = now.toLocalDate().plusDays(7);
        Category category = Category.valueOf(categoryKo);

        // 중복 생성 방지
        if (quizRepository.existsByTypeAndPublishedDateAndCategory(QuizType.WEEKDAY, published, category)) {
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
        return quizRepository.save(quiz).getQuizId();
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

    // 평일 오늘의 질문 추출
    public WeekdayQuizResDto getWeekdayQuiz(LocalDate date, String category, Long userId) {

        // 문자열로 받은 카테고리를 ENUM으로 변환
        Category cat = toCategory(category);

        // 오늘의 질문 조회
        Quiz quiz = quizRepository
                .findByOriginAndPublishedDateAndCategory(Origin.SYSTEM, date, cat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // 사용자의 응답 여부
        boolean completed = commentRepository.existsByQuiz_QuizIdAndUser_UserId(quiz.getQuizId(), userId);

        // TODO: 평일 오늘의 질문 반환 데이터 published_date 정제해서 반환, 오늘의 질문 채팅방 생성 시 채팅 ID 반환
        // 오늘의 질문 실제 반환
        return WeekdayQuizResDto.of(quiz, completed);
    }

    // 문자열로 받은 카테고리를 ENUM으로 변환
    private Category toCategory(String raw) {

        // 카테고리가 없을 때
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }

        try {
            // 한글 enum 그대로 매칭
            return Category.valueOf(raw.trim());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }
    }

    // TODO : 주말 오늘의 질문 추출

    // 채팅방 생성
    @Transactional
    public Long createChatRoom(ChatRoomFormDto form, Long userId) {
        // 유저 검증
        Users user = usersRepository.getReferenceById(userId);

        // form이 입력되지 않았을 때
        if (form == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_FORM");

        // 존재하지 않은 퀴즈 번호일 때
        if (form.getQuizId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_QUIZ_ID");

        // 오늘의 날짜
        LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 받은 퀴즈 id가 오늘의 퀴즈인지
        boolean isTodayQuiz = quizRepository.existsByQuizIdAndPublishedDate(form.getQuizId(), todaySeoul);

        // 오늘의 퀴즈가 아닐 때
        if (!isTodayQuiz) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QUIZ_NOT_TODAY");
        }

        // 1) 기존 채팅 존재 여부 확인 (chatId만 먼저)
        Optional<Long> existingIdOpt =
                aiChatRepository.findChatIdByQuizIdAndUserId(form.getQuizId(), userId);

        if (existingIdOpt.isPresent()) {
            Long chatId = existingIdOpt.get();

            if (form.getContent() != null && !form.getContent().isBlank()) {
                // TODO : 본격적인 채팅 구현
            }

            // 채팅방 ID 반환
            return chatId;
        }

        // 2) 없으면 새로 만들고 첫 AI 메시지 넣기
        Quiz quiz = quizRepository.findByQuizId(form.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "QUIZ_NOT_FOUND"));

        // Ai 채팅방 생성
        AiChat aiChat = new AiChat();
        aiChat.setUsers(user);
        aiChat.setQuiz(quiz);

        // 첫 번째 메시지를 퀴즈 내용으로 지정
        AiMessage aiMessage = new AiMessage();
        aiMessage.setOrigin(MessageOrigin.AI);
        aiMessage.setContent(quiz.getContent());
        aiMessage.setChat(aiChat);
        aiChat.getMessages().add(aiMessage);

        // 채팅방 ID 반환
        return aiChatRepository.save(aiChat).getChatId();
    }
}

