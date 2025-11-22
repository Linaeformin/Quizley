package com.example.quizley.service;
import com.example.quizley.domain.*;
import com.example.quizley.dto.community.*;
import com.example.quizley.entity.balance.BalanceAnswer;
import com.example.quizley.entity.balance.QuizBalance;
import com.example.quizley.entity.comment.Comment;
import com.example.quizley.entity.comment.CommentLike;
import com.example.quizley.entity.quiz.Quiz;
import com.example.quizley.entity.quiz.QuizLike;
import com.example.quizley.entity.users.BlockUser;
import com.example.quizley.entity.users.ReportUser;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.*;
import com.example.quizley.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityDetailService {
    private final QuizRepository quizRepository;
    private final QuizLikeRepository quizLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UsersRepository usersRepository;
    private final BalanceAnswerRepository balanceAnswerRepository;
    private final QuizBalanceRepository quizBalanceRepository;
    private final BlockUserRepository blockUserRepository;
    private final ReportUserRepository reportUserRepository;

    // 게시글 상세 조회
    public QuizDetailResponse getQuizDetail(Long quizId, Long currentUserId, String sort) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // 좋아요 수 조회
        Long likeCount = quizRepository.countLikesByQuizId(quizId);

        //댓글 수 조회
        Long commentCount = quizRepository.countCommentsByQuizId(quizId);

        // 사용자의 좋아요 여부 확인
        Boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = quizLikeRepository.existsByQuiz_QuizIdAndUser_UserId(quizId, currentUserId);
        }

        // 작성자 닉네임 조회 (익명 처리)
        String nickname = "익명";
        if (quiz.getUserId() != null && !quiz.getIsAnonymous()) {
            Users user = usersRepository.findById(quiz.getUserId()).orElse(null);
            if (user != null) {
                nickname = user.getNickname();
            }
        }

        //QuizDetailDto 생성
        QuizDetailDto quizDetail = QuizDetailDto.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .nickname(nickname)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(TimeFormatUtil.formatTimeAgo(quiz.getCreatedAt()))
                .isLiked(isLiked)
                .origin(quiz.getOrigin())
                .canLike(quiz.getOrigin() == Origin.USER)  // USER 질문만 좋아요 가능
                .canComment(quiz.getOrigin() == Origin.USER)  // USER 질문만 댓글 작성 가능
                .build();

        // 댓글 목록 조회
        List<CommentDto> comments = getCommentsWithTimeFormat(quizId, currentUserId, sort);

        return QuizDetailResponse.builder()
                .quiz(quizDetail)
                .comments(comments)
                .build();
    }

    // 주말 퀴즈 상세 조회
    public WeekendQuizDetailResponse getWeekendQuizDetail(Long quizId, Long currentUserId, String sort) {
        // 퀴즈 조회 및 검증
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // SYSTEM 주말 퀴즈인지 검증
        if (quiz.getOrigin() != Origin.SYSTEM || quiz.getType() != QuizType.WEEKEND) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_WEEKEND_QUIZ");
        }

        // 투표 결과 조회 (currentUserId 전달)
        WeekendQuizVoteResultDto voteResult = getVoteResult(quizId, currentUserId);

        // 댓글 목록 조회
        List<CommentDto> comments = getCommentsWithTimeFormat(quizId, currentUserId, sort);

        // 응답 생성
        return WeekendQuizDetailResponse.builder()
                .quizId(quiz.getQuizId())
                .content(quiz.getContent())
                .publishedDate(quiz.getPublishedDate())
                .voteResult(voteResult)
                .comments(comments)
                .build();
    }

    // 투표 결과 조회
    private WeekendQuizVoteResultDto getVoteResult(Long quizId, Long currentUserId) {
        List<QuizBalance> balances = quizBalanceRepository.findByQuizIdOrderBySideAsc(quizId);

        if (balances.size() != 2) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_BALANCE_DATA");
        }

        // 수정: ENUM 비교로 변경
        QuizBalance sideA = balances.stream()
                .filter(b -> BalanceSide.A.equals(b.getSide()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SIDE_A_NOT_FOUND"));

        // 수정: ENUM 비교로 변경
        QuizBalance sideB = balances.stream()
                .filter(b -> BalanceSide.B.equals(b.getSide()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SIDE_B_NOT_FOUND"));

        // 투표 결과 집계
        List<BalanceAnswer> allAnswers = balanceAnswerRepository.findByQuizId(quizId);

        Map<String, Long> voteMap = new HashMap<>();
        voteMap.put("A", 0L);
        voteMap.put("B", 0L);

        // 수정: ENUM의 name() 사용
        for (BalanceAnswer answer : allAnswers) {
            String side = answer.getSide().name(); // A 또는 B
            voteMap.put(side, voteMap.getOrDefault(side, 0L) + 1);
        }

        // 퍼센트 계산
        long totalVotes = allAnswers.size();
        Long sideACount = voteMap.get("A");
        Long sideBCount = voteMap.get("B");

        int sideAPercentage = totalVotes > 0 ? (int) Math.round((sideACount * 100.0) / totalVotes) : 0;
        int sideBPercentage = totalVotes > 0 ? (int) Math.round((sideBCount * 100.0) / totalVotes) : 0;

        // 현재 사용자가 선택한 항목 조회
        String userSelectedSide = null;
        if (currentUserId != null) {
            Optional<BalanceAnswer> userAnswer = balanceAnswerRepository.findByQuizIdAndUserId(quizId, currentUserId);
            if (userAnswer.isPresent()) {
                // TODO : side 형 변환
                userSelectedSide = userAnswer.get().getSide().name(); // "A" 또는 "B"
            }
        }

        // DTO 생성
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


    // 댓글 목록 조회
    private List<CommentDto> getCommentsWithTimeFormat(Long quizId, Long currentUserId, String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            // 인기순 (좋아요 많은 순) 쿼리 호출
            return commentRepository.findCommentsByQuizIdOrderByPopular(quizId, currentUserId);
        }

        // 기본값 latest(최신순) 쿼리 호출
        return commentRepository.findCommentsByQuizIdOrderByLatest(quizId, currentUserId);
    }

    // 퀴즈 좋아요
    @Transactional
    public void selectQuizLike(Long quizId, Long userId) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // SYSTEM 퀴즈는 좋아요 불가
        if (quiz.getOrigin() == Origin.SYSTEM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_LIKE_SYSTEM_QUIZ");
        }

        // 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // 좋아요 존재 여부 확인
        var existingLike = quizLikeRepository.findByQuiz_QuizIdAndUser_UserId(quizId, userId);
        if (existingLike.isPresent()) {
            // 좋아요 취소
            quizLikeRepository.delete(existingLike.get());
        } else {
            // 좋아요 추가
            QuizLike quizLike = QuizLike.builder()
                    .quiz(quiz)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            quizLikeRepository.save(quizLike);
        }
    }

    // 댓글 작성
    @Transactional
    public Long createComment(Long quizId, CommentCreateDto commentCreateDto, Long userId) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // SYSTEM 퀴즈는 댓글 작성 불가
        if (quiz.getOrigin() == Origin.SYSTEM && quiz.getType() == QuizType.WEEKDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_COMMENT_ON_SYSTEM_QUIZ");
        }

        // 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // 댓글 생성
        Comment comment = new Comment();
        comment.setQuiz(quiz);
        comment.setUser(user);
        comment.setContent(commentCreateDto.getContent());
        comment.setWriterAnonymous(commentCreateDto.getIsAnonymous() != null && commentCreateDto.getIsAnonymous());
        comment.setStatus(com.example.quizley.domain.Status.DONE);
        comment.setCommentAnonymous(com.example.quizley.domain.CommentAnonymous.OPEN);
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setModifiedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return savedComment.getCommentId();
    }

    // 댓글 좋아요 선택
    @Transactional
    public void selectCommentLike(Long commentId, Long userId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        // 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // 좋아요 존재 여부 확인
        var existingLike = commentLikeRepository.findByComment_CommentIdAndUser_UserId(commentId, userId);
        if (existingLike.isPresent()) {
            // 좋아요 취소
            commentLikeRepository.delete(existingLike.get());
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            // 좋아요 증가
            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            commentLikeRepository.save(commentLike);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        commentRepository.save(comment);
    }

    // 게시글 작성
    @Transactional
    public Long createUserQuiz(QuizCreateDto dto, Long userId) {
        // 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        // 익명 여부 처리 (null이면 false로 기본값 설정)
        Boolean isAnonymous = dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false;

        // Quiz 엔티티 생성
        Quiz quiz = Quiz.builder()
                .origin(Origin.USER)
                .type(QuizType.WEEKDAY)
                .content(dto.getContent())
                .category(dto.getCategory())
                .userId(userId)
                .isAnonymous(isAnonymous)
                .publishedDate(LocalDate.now())  // 오늘 날짜로 공개
                .build();

        // 퀴즈 저장
        Quiz savedQuiz = quizRepository.save(quiz);
        return savedQuiz.getQuizId();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        // 본인이 작성한 댓글인지 확인
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // soft delete
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    // 다른 유저의 댓글 신고
    @Transactional
    public void reportComment(Long commentId, Long userId) {
        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        // 이미 신고한 댓글인지 확인
        boolean alreadyReported = reportUserRepository.existsByReporterIdAndContentTypeAndContentId(
                userId, ContentType.COMMENT, commentId
        );

        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_REPORTED");
        }

        // 신고 생성
        ReportUser report = ReportUser.builder()
                .reporterId(userId)
                .contentType(ContentType.COMMENT)
                .contentId(commentId)
                .action(ReportAction.NONE)
                .createdAt(LocalDateTime.now())
                .build();

        reportUserRepository.save(report);
    }

    // 사용자 차단
    @Transactional
    public void blockUser(Long blockedUserId, Long currentUserId) {
        // 자기 자신을 차단할 수 없음
        if (blockedUserId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_BLOCK_YOURSELF");
        }

        // 차단할 사용자 존재 확인
        usersRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
        // 이미 차단했는지 확인
        boolean alreadyBlocked = blockUserRepository.existsByBlockerIdAndBlockedId(currentUserId, blockedUserId);

        if (alreadyBlocked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_BLOCKED");
        }

        // 차단 생성
        BlockUser blockUser = BlockUser.builder()
                .blockerId(currentUserId)
                .blockedId(blockedUserId)
                .createdAt(LocalDateTime.now())
                .build();

        blockUserRepository.save(blockUser);
    }

    // 커뮤니티 게시물 수정
    @Transactional
    public void updateUserQuiz(Long quizId, QuizCreateDto dto, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // 본인이 작성한 게시물인지 확인
        if (!quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // 댓글이 달렸는지 확인
        Long commentCount = quizRepository.countCommentsByQuizId(quizId);
        if (commentCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_EDIT_QUIZ_WITH_COMMENTS");
        }

        // 게시글 수정
        quiz.setContent(dto.getContent());
        quiz.setCategory(dto.getCategory());
        if (dto.getIsAnonymous() != null) {
            quiz.setIsAnonymous(dto.getIsAnonymous());
        }

        quizRepository.save(quiz);
    }

    // 작성한 게시물 삭제

    @Transactional
    public void deleteUserQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        // 본인이 작성한 게시물인지 확인
        if (!quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        quizRepository.delete(quiz);

    }

    // 게시물 신고
    @Transactional
    public void reportQuiz(Long quizId, Long userId) {
        // 게시물 존재 확인
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        // 본인 게시물은 신고 불가
        if (quiz.getUserId() != null && quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_REPORT_OWN_QUIZ");
        }

        // 이미 신고했는지 확인
        boolean alreadyReported = reportUserRepository.existsByReporterIdAndContentTypeAndContentId(
                userId, ContentType.QUESTION, quizId
        );

        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_REPORTED");
        }

        // 신고 생성
        ReportUser report = ReportUser.builder()
                .reporterId(userId)
                .contentType(ContentType.QUESTION)
                .contentId(quizId)
                .action(ReportAction.NONE)
                .createdAt(LocalDateTime.now())
                .build();

        reportUserRepository.save(report);
    }
}
