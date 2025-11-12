package com.example.quizley.service;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import com.example.quizley.dto.community.CommunityHomeResponse;
import com.example.quizley.dto.community.HotQuizDto;
import com.example.quizley.dto.community.QuizListDto;
import com.example.quizley.dto.community.TodayQuizDto;
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
        //오늘의 질문 조회
        Quiz todayQuiz = getTodayQuiz(date);
        Boolean isTodayQuizLiked = checkQuizLiked(todayQuiz.getQuizId(), currentUserId);

        //카테고리별 핫 게시글
        Map<String, List<HotQuizDto>> hotQuizzesByCategory = getHotQuizzesByCategory(date, category, currentUserId);

        //이란 게시글 목록 조회(최신순)
        List<QuizListDto> quizzes = getQuizList(date, "latest", category, currentUserId);

        //응답 생성
        return CommunityHomeResponse.builder()
                .date(date)
                .todayQuiz(convertToTodayQuizDto(todayQuiz, isTodayQuizLiked))
                .quizzesByCategory(hotQuizzesByCategory)
                .quizzes(quizzes)
                .build();
        }

    //게시글 목록 조회
    public List<QuizListDto> getQuizList(LocalDate date, String sortBy, Category category, Long currentUserId) {
        validateDate(date);
        validateSortType(sortBy);

        //Repository에서 DTO 조회
        List<QuizListDto> quizzes = (category != null)
                ? quizRepository.findQuizListByCategory(date, Origin.USER, category, currentUserId)
                : quizRepository.findAllQuizList(date, Origin.USER, currentUserId);

        //정렬
        if ("popular".equals(sortBy)) {
            return sortByPopularity(quizzes);
        }
        return quizzes;
    }

    //오늘의 질문 조회
    private Quiz getTodayQuiz(LocalDate date) {
        return quizRepository.findByPublishedDateAndOrigin(date, Origin.SYSTEM)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TODAY_QUIZ_NOT_FOUND"));
    }

    //퀴즈 좋아요 여부 확인
    private Boolean checkQuizLiked(Long quizId, Long currentUserId) {
        if (currentUserId == null) {
            return false;
        }

        Boolean isLiked = quizRepository.isQuizLikedByUser(quizId, currentUserId);
        return isLiked != null ? isLiked : false;
    }

    //카테고리별 HOT 게시글 조회
    private Map<String, List<HotQuizDto>> getHotQuizzesByCategory(
            LocalDate date, Category category, Long currentUserId) {

        Map<String, List<HotQuizDto>> result = new LinkedHashMap<>();

        List<Category> categories = (category != null)
                ? List.of(category)
                : Arrays.asList(Category.values());

        //각 카테고리별 핫 게시글 조회 (상위 3개)
        for (Category cat : categories) {
            List<HotQuizDto> hotQuizzes = quizRepository.findHotQuizzesByCategory(
                    date, Origin.USER, cat, currentUserId, PageRequest.of(0, 3)
            );
            result.put(cat.name(), hotQuizzes);
        }
        return result;
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
                .publishDate(quiz.getPublishedDate())
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
}
