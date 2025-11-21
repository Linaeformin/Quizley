package com.example.quizley.service;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import com.example.quizley.dto.community.*;
import com.example.quizley.entity.quiz.Quiz;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final QuizRepository quizRepository;

    //커뮤니티 홈 화면 조회
    public CommunityHomeResponse getCommunityHome(LocalDate date, Category category, Long currentUserId) {
        validateDate(date);

        if(category == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
        }

        //오늘의 질문 조회
        Quiz todayQuiz = getTodayQuiz(date, category);
        Boolean isTodayQuizLiked = checkQuizLiked(todayQuiz.getQuizId(), currentUserId);
        TodayQuizDto todayQuizDto = convertToTodayQuizDto(todayQuiz, isTodayQuizLiked);

        //특정 카테고리의 HOT 게시글 3개
        List<HotQuizDto> hotQuiz = getHotQuizzesByCategory(date, category, currentUserId);

        //특정 카테고리의 사용자 퀴즈만
        List<QuizListDto> quizzes = getQuizList(date, "latest", category, currentUserId);

        //응답 생성
        return CommunityHomeResponse.builder()
                .date(date)
                .category(category.name())
                .hotQuiz(hotQuiz)
                .todayQuiz(todayQuizDto)
                .quizzes(quizzes) //특정 카테고리의 전체 목록
                .build();
        }

    //게시글 목록 조회
    public List<QuizListDto> getQuizList(LocalDate date, String sortBy, Category category, Long currentUserId) {
        validateDate(date);
        validateSortType(sortBy);

        //사용자 퀴즈만 가져오기
        List<QuizListDto> userQuizzes = quizRepository.findQuizListByCategory(
                date, Origin.USER, category, currentUserId
        );

        //정렬
        if ("popular".equals(sortBy)) {
            userQuizzes = sortByPopularity(userQuizzes);
        }
        return userQuizzes; //사용자 퀴즈만 반환
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

    //카테고리별 HOT 게시글 조회
    private List<HotQuizDto> getHotQuizzesByCategory(LocalDate date, Category category, Long currentUserId) {
        return quizRepository.findHotQuizzesByCategory(
                date, Origin.USER, category, currentUserId, PageRequest.of(0, 3)
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
    private TodayQuizDto convertToTodayQuizDto(Quiz quiz, Boolean isLiked) {
        return TodayQuizDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .category(quiz.getCategory().name())
                .publishedDate(quiz.getPublishedDate())
                .isLiked(isLiked)
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
}
