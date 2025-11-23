package com.example.quizley.service;

import com.example.quizley.common.level.LevelService;
import com.example.quizley.common.level.LevelUpResponse;
import com.example.quizley.domain.*;
import com.example.quizley.dto.quiz.*;
import com.example.quizley.entity.balance.BalanceAnswer;
import com.example.quizley.entity.balance.QuizBalance;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.quiz.AiChat;
import com.example.quizley.entity.quiz.AiMessage;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.*;
import com.example.quizley.storage.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;


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
    private final ChatService chatService;
    private final QuizBalanceRepository quizBalanceRepository;
    private final LevelService levelService;
    private final BalanceAnswerRepository balanceAnswerRepository;
    private final AttendanceService attendanceService;
    private final Integer ANSWER_POINT = 100;

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

        // 채팅방 생성 날짜 및 요일 포맷
        DateTimeFormatter roomDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
                .withLocale(Locale.KOREAN);

        String roomDate = quiz.getPublishedDate().format(roomDateFormatter);

        // 레벨업
        attendanceService.attendToday(userId);

        // 오늘의 질문 실제 반환
        return WeekdayQuizResDto.of(quiz, completed, roomDate);
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

    // 채팅방 생성
    @Transactional
    public Long createChatRoom(ChatRoomFormDto form, Long userId) {
        // 유저 검증
        Users user = usersRepository.getReferenceById(userId);

        // form이 입력되지 않았을 때
        if (form == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_FORM");

        // 존재하지 않은 퀴즈 번호일 때
        Quiz quiz = quizRepository.findByQuizId(form.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "QUIZ_NOT_FOUND"));

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
                // 채팅 메시지 전송
                chatService.chat(chatId, userId, form.getContent());
            }

            // 채팅방 ID 반환
            return chatId;
        }

        // 2) 없으면 새로 만들고 첫 AI 메시지 넣기
        // Ai 채팅방 생성
        AiChat aiChat = new AiChat();
        aiChat.setUsers(user);
        aiChat.setQuiz(quiz);

        // 첫 번째 메시지를 퀴즈 내용으로 지정
        AiMessage aiMessage = new AiMessage();
        aiMessage.setOrigin(MessageOrigin.AI);
        aiMessage.setContent(quiz.getContent());
        aiChat.addMessage(aiMessage);

        Long chatId = aiChatRepository.save(aiChat).getChatId();

        if (form.getContent() != null && !form.getContent().isBlank()) {
            // 채팅 메시지 전송
            chatService.chat(chatId, userId, form.getContent());
        }

        // 채팅방 ID 반환
        return chatId;
    }

    // 채팅방 접속 시 메시지 데이터 반환
    public ChatRoomResDto getMessage(Long chatId, Long userId, int page, int size) {
        // 1. 채팅방 존재 여부 체크
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_FOUND"));

        // 2. 채팅방 주인인지 체크
        if (!chat.getUsers().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FORBIDDEN");
        }

        // 채팅 퀴즈 객체
        Quiz quiz = chat.getQuiz();

        // 1) page 하한 검증: -1보다 작으면 에러
        if (page < -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PAGE");
        }

        // 2) 전체 페이지 개수 계산
        long totalElements = aiMessageRepository.countByChatChatId(chatId);
        int totalPages = (totalElements == 0) ? 0 : (int) Math.ceil((double) totalElements / size);

        // 마지막 페이지 인덱스
        int maxPage = (totalPages == 0) ? 0 : totalPages - 1;

        // 3) 이론상 메시지가 없는 채팅방은 생성되지 않지만, 방어적으로 처리
        if (totalPages == 0) {
            // 메시지 없으면 page는 -1 또는 0만 허용
            if (page > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_FOUND");
            }

            // 채팅방 생성 날짜 및 요일
            DateTimeFormatter roomDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
                    .withLocale(Locale.KOREAN);

            // 데이터 반환
            return ChatRoomResDto.builder()
                    .chatId(chat.getChatId())
                    .category(quiz.getCategory().name())
                    .date(quiz.getPublishedDate().format(roomDateFormatter))
                    .messages(List.of())
                    .totalPages(0)
                    .maxPages(0)
                    .currentPage(0)
                    .hasPrev(false)
                    .hasNext(false)
                    .build();
        }

        // 4) 메시지가 있을 때 page 상한 검증
        if (page > maxPage) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_FOUND");
        }

        // page가 -1이면 "최신 페이지" = 0
        if (page < 0) {
            page = 0;
        }

        // 5) 실제 페이지 최신순 조회, createdAt이 같다면 PK순으로
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "messageId"));

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AiMessage> messagePage = aiMessageRepository.findByChatChatId(chatId, pageable);

        // 현재 페이지 번호
        int currentPage = messagePage.getNumber();

        // 채팅방 생성 일자 및 요일 반환
        DateTimeFormatter roomDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
                .withLocale(Locale.KOREAN);

        // 메시지 생성 시각 반환
        DateTimeFormatter messageDateFormatter = DateTimeFormatter.ofPattern("a hh:mm")
                .withLocale(Locale.KOREAN);

        // DESC로 가져온 뒤 ASC로 뒤집기
        List<AiMessage> messages = new java.util.ArrayList<>(messagePage.getContent());
        java.util.Collections.reverse(messages);

        // 메시지 데이터 삽입
        List<ChatMessageResDto> messageDtos = messages.stream()
                .map(m -> ChatMessageResDto.builder()
                        .origin(m.getOrigin())
                        .message(m.getContent())
                        .date(m.getCreatedAt().format(messageDateFormatter))
                        .build()
                )
                .toList();

        // 이전 페이지 존재 여부
        boolean hasPrev = currentPage > 0;

        // 다음 페이지 존재 여부
        boolean hasNext = currentPage < maxPage;

        // 채팅방 및 채팅 메시지 데이터 반환
        return ChatRoomResDto.builder()
                .chatId(chat.getChatId())
                .category(quiz.getCategory().name())
                .date(quiz.getPublishedDate().format(roomDateFormatter))
                .messages(messageDtos)
                .totalPages(totalPages)
                .maxPages(maxPage)
                .currentPage(currentPage)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .build();
    }

    // 답변 완료 처리
    @Transactional
    public void completeComment(Long chatId, Long userId) {

        // 1. 채팅방 존재 여부 체크
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CHAT_NOT_FOUND"));

        // 2. 채팅방 주인인지 체크
        if (!chat.getUsers().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // 3. 채팅에 연결된 퀴즈 가져오기
        Quiz quiz = chat.getQuiz();

        // 4. 해당 퀴즈 + 유저로 댓글 찾기
        Comment comment = commentRepository.findByQuiz_QuizIdAndUser_UserId(quiz.getQuizId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        // 5. 이미 DONE이면 그냥 리턴
        if (comment.getStatus() == Status.DONE) {
            return;
        }

        // 6. 상태 변경 + 수정 시간 갱신
        comment.setStatus(Status.DONE);
        comment.setModifiedAt(LocalDateTime.now(ZONE));

        // 레벨업
        levelService.tryLevelUp(userId, ANSWER_POINT);
    }

    // [홈] 오늘의 질문 답변 커뮤니티 공유
    @Transactional
    public void share(Long chatId, Long userId, ChatCommentOpenFormDto dto) {
        // 1. 채팅방 존재 여부 체크
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CHAT_NOT_FOUND"));

        // 2. 채팅방 주인인지 체크
        if (!chat.getUsers().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // 3. 채팅에 연결된 퀴즈 가져오기
        Quiz quiz = chat.getQuiz();

        // 4. 해당 퀴즈 + 유저로 댓글 찾기 (soft delete 고려)
        Comment comment = commentRepository
                .findByQuiz_QuizIdAndUser_UserIdAndDeletedAtIsNull(quiz.getQuizId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        // 5. 아직 답변 진행 중이면 공유 불가 (선택)
        if (comment.getStatus() != Status.DONE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "COMMENT_NOT_DONE");
        }

        // 6. 공개 / 비공개 설정
        // comment_anonymous == true → OPEN(공개), false → CLOSE(비공개)라고 가정
        comment.setCommentAnonymous(
                dto.isComment_anonymous() ? CommentAnonymous.OPEN : CommentAnonymous.CLOSE
        );

        // 7. 작성자 익명 여부
        comment.setWriterAnonymous(dto.isWriter_anonymous());
    }

    // 퀴즐리봇 요약 수정
    @Transactional
    public void editSummary(Long chatId, Long userId, ChatSummaryFormDto dto) {
        // 1) 채팅 찾기
        AiChat aiChat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_FOUND"));

        // 2) 해당 채팅의 주인인지 체크
        if (!aiChat.getUsers().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FORBIDDEN");
        }

        // 요약 메시지 수정
        aiChat.setSummary(dto.getSummary());
    }

    // 주말 오늘의 밸런스 질문 추출
    public WeekendQuizResDto getWeekendQuiz(LocalDate date, Long userId) {

        // 오늘의 주말 퀴즈 조회 (SYSTEM + WEEKEND + 날짜)
        Quiz quiz = quizRepository
                .findFirstByOriginAndTypeAndPublishedDate(
                        Origin.SYSTEM,
                        QuizType.WEEKEND,
                        date
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // 사용자의 응답 여부 (주말 밸런스 답변 기록 기준)
        boolean completed = balanceAnswerRepository
                .existsByQuizIdAndUserId(quiz.getQuizId(), userId);

        // 채팅방 생성 날짜 및 요일 포맷
        DateTimeFormatter roomDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
                .withLocale(Locale.KOREAN);

        String roomDate = quiz.getPublishedDate().format(roomDateFormatter);

        // 밸런스 선택지 조회 (A/B)
        List<QuizBalance> balances = quizBalanceRepository
                .findByQuizIdOrderBySideAsc(quiz.getQuizId());

        if (balances.size() != 2) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_BALANCE_CONFIG");
        }

        // 레벨업
        attendanceService.attendToday(userId);

        // 주말 오늘의 밸런스 질문 실제 반환
        return WeekendQuizResDto.of(quiz, completed, roomDate, balances);
    }

    // 주말 밸런스 게임 투표
    @Transactional
    public void vote(Long userId, BalanceVoteFormDto dto) {

        Long quizId = dto.getQuizId();
        BalanceSide side = dto.getSide();

        // 1) 퀴즈 존재 여부 체크
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // 2) 밸런스 게임 타입인지 검증
        if (quiz.getType() != QuizType.WEEKEND) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NOT_WEEKEND");
        }

        // 2) 이미 투표한 유저인지 체크
        if (balanceAnswerRepository.existsByQuizIdAndUserId(quizId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ALREADY_VOTED");
        }

        // 3) 투표 저장
        BalanceAnswer answer = new BalanceAnswer();
        answer.setQuizId(quizId);
        answer.setUserId(userId);
        answer.setSide(side);
        answer.setCreatedAt(LocalDateTime.now());

        balanceAnswerRepository.save(answer);

        // 레벨업
        levelService.tryLevelUp(userId, ANSWER_POINT);
    }
}

