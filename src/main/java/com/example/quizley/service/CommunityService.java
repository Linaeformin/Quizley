package com.example.quizley.service;

import com.example.quizley.domain.BalanceSide;
import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import com.example.quizley.domain.QuizType;
import com.example.quizley.dto.community.*;
import com.example.quizley.entity.balance.BalanceAnswer;
import com.example.quizley.entity.balance.QuizBalance;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.repository.BalanceAnswerRepository;
import com.example.quizley.repository.CommentRepository;
import com.example.quizley.repository.QuizBalanceRepository;
import com.example.quizley.repository.QuizRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.codehaus.groovy.util.ListHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.quizley.entity.comment.Comment;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final QuizRepository quizRepository;
    private final QuizBalanceRepository quizBalanceRepository;
    private final BalanceAnswerRepository balanceAnswerRepository;
    private final CommentRepository commentRepository;

    public CommunityHomeResponse getCommunityHome(LocalDate date, Category category, Long currentUserId) {
        validateDate(date);

        if(category == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "USE_WEEKEND_ENDPOINT");
        }

        //오늘의 질문 조회
        Quiz todayQuiz = quizRepository
                .findByOriginAndTypeAndPublishedDateAndCategory(
                        Origin.SYSTEM,
                        QuizType.WEEKDAY,
                        date,
                        category
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));
        Boolean isTodayQuizLiked = checkQuizLiked(todayQuiz.getQuizId(), currentUserId);
        Long commentCount = quizRepository.countCommentsByQuizId(todayQuiz.getQuizId());
        Boolean isMine = (currentUserId != null && todayQuiz.getUserId() != null
                && todayQuiz.getUserId().equals(currentUserId));

        TodayQuizDto todayQuizDto = convertToTodayQuizDto(todayQuiz, isTodayQuizLiked, commentCount, isMine);

        // HOT 게시글 3개
        List<HotQuizDto> hotQuiz = getHotQuizzesByCategory(date, category, currentUserId);

        // 사용자 퀴즈 목록
        List<QuizListDto> quizzes = getQuizList(date, "latest", category, currentUserId);

        return CommunityHomeResponse.builder()
                .date(date)
                .category(category.name())
                .hotQuiz(hotQuiz)
                .todayQuiz(todayQuizDto)
                .quizzes(quizzes)
                .build();
    }

    // 게시글 목록 조회
    public List<QuizListDto> getQuizList(LocalDate date, String sortBy, Category category, Long currentUserId) {
        validateDate(date);
        validateSortType(sortBy);

        System.out.println("=== getQuizList DEBUG ===");
        System.out.println("date: " + date);
        System.out.println("origin: " + Origin.USER);
        System.out.println("category: " + category);
        System.out.println("userId: " + currentUserId);

        List<QuizListDto> userQuizzes;

        // 카테고리가 있으면 카테고리별 조회, 없으면 전체 조회
        if (category != null) {
            userQuizzes = quizRepository.findQuizListByCategory(
                    date, Origin.USER, category, currentUserId
            );
        } else {
            userQuizzes = quizRepository.findAllQuizList(
                    date, Origin.USER, currentUserId
            );
        }

        // 정렬
        if ("popular".equals(sortBy)) {
            userQuizzes = sortByPopularity(userQuizzes);
        }

        return userQuizzes;
    }

    //오늘의 질문 조회
    private Quiz getTodayQuiz(LocalDate date, Category category) {
        return quizRepository.findByOriginAndPublishedDateAndCategory(
                Origin.SYSTEM, date, category)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TODAY_QUIZ_NOT_FOUND"));

    }

    //퀴즈 좋아요 여부 확인
    private Boolean checkQuizLiked(Long quizId, Long currentUserId) {
        if(currentUserId == null){
            return false;
        }
        Boolean isLiked = quizRepository.isQuizLikedByUser(quizId, currentUserId);
        return isLiked != null ? isLiked:false;
    }

    // 카테고리별 HOT 게시글 조회
    private List<HotQuizDto> getHotQuizzesByCategory(LocalDate date, Category category, Long currentUserId) {
        return quizRepository.findTop3ByDateAndCategoryOrderByLikes(
                date,
                Origin.USER,
                category,
                currentUserId,
                PageRequest.of(0, 3)
        );
    }

    //인기순 정렬
    private List<QuizListDto> sortByPopularity(List<QuizListDto> quizzes) {
        return quizzes.stream()
                .sorted((q1, q2) -> {
                    Long popularity1 = q1.getLikeCount() + q1.getCommentCount();
                    Long popularity2 = q2.getLikeCount() + q2.getCommentCount();
                    return popularity2.compareTo(popularity1);
                })
                .collect(Collectors.toList());
    }

    //퀴즈 엔티티를 TodayQuizDto로 변환
    private TodayQuizDto convertToTodayQuizDto(Quiz quiz, Boolean isLiked, Long commentCount, Boolean isMine) {
        return TodayQuizDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .category(quiz.getCategory().name())
                .publishedDate(quiz.getPublishedDate())
                .isLiked(isLiked)
                .commentCount(commentCount)
                .isMine(isMine)  // 추가
                .build();
    }

    //날짜 유효성 검증
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_DATE");
        }
    }

    //정렬 타입 유효성 검증
    private void validateSortType(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_SORT_TYPE");
        }

        if (!"latest".equals(sortBy) && !"popular".equals(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_SORT_TYPE");
        }
    }

    // 키워드로 퀴즈 검색
    public QuizSearchResponse searchQuizzes(String keyword, String sortBy, Long currentUserId) {
        // 키워드 유효성 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_KEYWORD");
        }

        // 정렬 타입 유효성 검증
        validateSortType(sortBy);

        // 키워드로 퀴즈 검색 (최신순)
        List<QuizListDto> searchResults = quizRepository.searchQuizzesByKeywordOrderByLatest(
                keyword.trim(), currentUserId
        );

        // 인기순 정렬이 요청된 경우
        if ("popular".equals(sortBy)) {
            searchResults = sortByPopularity(searchResults);
        }

        // 전체 검색 결과 개수 조회
        Long totalCount = quizRepository.countByContentContaining(keyword.trim());

        // 응답 생성
        return QuizSearchResponse.builder()
                .keyword(keyword.trim())
                .totalCount(totalCount)
                .quizzes(searchResults)
                .build();
    }

    // 주말 홈 화면 조회 메서드
    public WeekendCommunityHomeResponse getWeekendCommunityHome(LocalDate date, Category category, String sortBy, Long currentUserId) {
        validateSortType(sortBy);  // 추가

        // 주말 퀴즈 조회
        Quiz weekendQuiz = quizRepository
                .findFirstByOriginAndTypeAndPublishedDate(Origin.SYSTEM, QuizType.WEEKEND, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WEEKEND_QUIZ_NOT_FOUND"));

        // 투표 결과 조회
        WeekendQuizVoteResultDto voteResult = getVoteResult(weekendQuiz.getQuizId(), currentUserId);

        // 주말 퀴즈 DTO 생성
        WeekendQuizDto weekendQuizDto = WeekendQuizDto.builder()
                .quizId(weekendQuiz.getQuizId())
                .content(weekendQuiz.getContent())
                .publishedDate(weekendQuiz.getPublishedDate())
                .voteResult(voteResult)
                .build();

        // 카테고리별 HOT 게시글 3개 (평일과 동일)
        List<HotQuizDto> hotQuiz = getHotQuizzesByCategory(date, category, currentUserId);

        // 카테고리별 사용자 퀴즈 목록 (평일과 동일) - sortBy 적용
        List<QuizListDto> quizzes = getQuizList(date, sortBy, category, currentUserId);

        return WeekendCommunityHomeResponse.builder()
                .date(date)
                .category(category.name())
                .weekendQuiz(weekendQuizDto)
                .hotQuiz(hotQuiz)
                .quizzes(quizzes)
                .build();
    }

    // CommunityDetailService에서 가져오기
    private WeekendQuizVoteResultDto getVoteResult(Long quizId, Long currentUserId) {
        List<QuizBalance> balances = quizBalanceRepository.findByQuizIdOrderBySideAsc(quizId);

        if (balances.size() != 2) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_BALANCE_DATA");
        }

        QuizBalance sideA = balances.stream()
                .filter(b -> BalanceSide.A.equals(b.getSide()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SIDE_A_NOT_FOUND"));

        QuizBalance sideB = balances.stream()
                .filter(b -> BalanceSide.B.equals(b.getSide()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SIDE_B_NOT_FOUND"));

        List<BalanceAnswer> allAnswers = balanceAnswerRepository.findByQuizId(quizId);

        Map<String, Long> voteMap = new HashMap<>();
        voteMap.put("A", 0L);
        voteMap.put("B", 0L);

        for (BalanceAnswer answer : allAnswers) {
            String side = answer.getSide().name();
            voteMap.put(side, voteMap.getOrDefault(side, 0L) + 1);
        }

        long totalVotes = allAnswers.size();
        Long sideACount = voteMap.get("A");
        Long sideBCount = voteMap.get("B");

        int sideAPercentage = totalVotes > 0 ? (int) Math.round((sideACount * 100.0) / totalVotes) : 0;
        int sideBPercentage = totalVotes > 0 ? (int) Math.round((sideBCount * 100.0) / totalVotes) : 0;

        String userSelectedSide = null;
        if (currentUserId != null) {
            Optional<BalanceAnswer> userAnswer = balanceAnswerRepository.findByQuizIdAndUserId(quizId, currentUserId);
            if (userAnswer.isPresent()) {
                userSelectedSide = userAnswer.get().getSide().name();
            }
        }

        return WeekendQuizVoteResultDto.builder()
                .sideALabel(sideA.getLabel())
                .sideAImageUrl(sideA.getImgUrl())
                .sideAPercentage(sideAPercentage)
                .sideBLabel(sideB.getLabel())
                .sideBImageUrl(sideB.getImgUrl())
                .sideBPercentage(sideBPercentage)
                .userSelectedSide(userSelectedSide)
                .build();
    }
}
