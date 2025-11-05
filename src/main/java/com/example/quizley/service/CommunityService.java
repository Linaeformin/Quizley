package com.example.quizley.service;

import com.example.quizley.domain.Category;
import com.example.quizley.domain.Origin;
import com.example.quizley.dto.community.CommunityHomeResponse;
import com.example.quizley.dto.community.HotQuizDto;
import com.example.quizley.dto.community.QuizListDto;
import com.example.quizley.dto.community.TodayQuizDto;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final QuizRepository quizRepository;

    //커뮤니티 홈 화면 조회
    public CommunityHomeResponse getCommunityHome(LocalDate date) {
        //오늘의 질문 조회
        Quiz todayQuiz = quizRepository.findByPublishedDateAndOrigin(date, Origin.SYSTEM)
                .orElseThrow(()-> new RuntimeException("오늘의 질문이 없습니다."));

        //카테고리별로 HOT 인기글 3개씩 조회
        Map<String, List<HotQuizDto>> hotQuizzesByCategory = new LinkedHashMap<>();

        //모든 카테고리 순회
        for (Category category : Category.values()) {
            List<Quiz> hotQuizzes = quizRepository
                    .findByPublishedDateAndOriginAndCategoryOrderByCreatedAtDesc(
                            date,
                            Origin.USER,
                            category,
                            PageRequest.of(0, 3) //상위 N개 수정 필요
                    );

            List<HotQuizDto> hotQuizDtos = hotQuizzes.stream()
                    .map(this::convertToHotQuizDto)
                    .collect(Collectors.toList());

            //카테고리 이름을 문자열로 변환(한글로)
            hotQuizzesByCategory.put(category.name(), hotQuizDtos);
        }

        //응답 생성
        return CommunityHomeResponse.builder()
                .date(date)
                .todayQuiz(convertToTodayQuizDto(todayQuiz))
                .hotQuizzesByCategory(hotQuizzesByCategory)
                .build();
    }

    //게시글 목록 조회
    public List<QuizListDto> getQuizList(LocalDate date, String sortBy){
        //오늘의 질문(SYSTEM) 조회
        Optional<Quiz> todayQuizOpt=quizRepository.findByPublishedDateAndOrigin(date, Origin.SYSTEM);

        //사용자 질문(USER) 조회
        List<Quiz> userQuizzes;

        //최신순 정렬
        if("latest".equals(sortBy)){
            // 최신순: USER 질문만 조회
            userQuizzes = quizRepository.findByPublishedDateAndOriginOrderByCreatedAtDesc(date, Origin.USER);
        }

        //인기순 정렬(댓글 수 기반 정렬 추가 필요)
        else if("popular".equals(sortBy)){
            // 임시로 최신순 사용
            userQuizzes = quizRepository.findByPublishedDateAndOriginOrderByCreatedAtDesc(date, Origin.USER);
        } else {
            userQuizzes = quizRepository.findByPublishedDateAndOriginOrderByCreatedAtDesc(date, Origin.USER);
        }

        //SYSTEM을 맨 위에, USER를 아래에 배치
        List<QuizListDto> result = new ArrayList<>();

        todayQuizOpt.ifPresent(quiz -> result.add(convertToQuizListDto(quiz)));

        List<QuizListDto> userQuizDtos = userQuizzes.stream()
                .map(this::convertToQuizListDto)
                .collect(Collectors.toList());
        result.addAll(userQuizDtos);
        return result;
    }

    //Dto 변환 메서드들
    private TodayQuizDto convertToTodayQuizDto(Quiz quiz) {
        return TodayQuizDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .category(quiz.getCategory().name())
                .publishDate(quiz.getPublishedDate())
                .build();
    }

    private HotQuizDto convertToHotQuizDto(Quiz quiz) {
        return HotQuizDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .category(quiz.getCategory().name())
                .likeCount(0L) //Like 테이블 연동 후 실제 값으로 변경 필요
                .commentCount(0L) //Comment 테이블 연동 후 실제 값으로 변경 필요
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    private QuizListDto convertToQuizListDto(Quiz quiz) {
        return QuizListDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .category(quiz.getCategory().name())
                .likeCount(0L) //Like 테이블 연동 후 실제 값으로 변경 필요
                .commentCount(0L) //Comment 테이블 연동 후 실제 값으로 변경 필요
                .publishedDate(quiz.getPublishedDate())
                .build();
    }
}
