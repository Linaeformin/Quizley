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
    private final NotificationService notificationService;

    // 게시글 상세 조회
    public QuizDetailResponse getQuizDetail(Long quizId, Long currentUserId, String sort) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        Long likeCount = quizRepository.countLikesByQuizId(quizId);
        Long commentCount = quizRepository.countCommentsByQuizId(quizId);

        Boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = quizLikeRepository.existsByQuiz_QuizIdAndUser_UserId(quizId, currentUserId);
        }

        // ✅ 본인 여부: userId 기준으로 판별 (익명과 무관)
        Boolean isMine = (currentUserId != null
                && quiz.getUserId() != null
                && quiz.getUserId().equals(currentUserId));

        // 작성자 닉네임 조회 (익명 처리)
        String nickname = "익명";
        if (quiz.getUserId() != null && !quiz.getIsAnonymous()) {
            Users user = usersRepository.findById(quiz.getUserId()).orElse(null);
            if (user != null) {
                nickname = user.getNickname();
            }
        }

        QuizDetailDto quizDetail = QuizDetailDto.builder()
                .quizId(quiz.getQuizId())
                .userId(quiz.getUserId())
                .content(quiz.getContent())
                .nickname(nickname)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(TimeFormatUtil.formatTimeAgo(quiz.getCreatedAt()))
                .isLiked(isLiked)
                .origin(quiz.getOrigin())
                .canLike(quiz.getOrigin() == Origin.USER)
                .canComment(quiz.getOrigin() == Origin.USER)
                .isMine(isMine)
                .build();

        List<CommentDto> comments = getCommentsWithTimeFormat(quizId, currentUserId, sort);

        return QuizDetailResponse.builder()
                .quiz(quizDetail)
                .comments(comments)
                .build();
    }

    // 주말 퀴즈 상세 조회
    public WeekendQuizDetailResponse getWeekendQuizDetail(Long quizId, Long currentUserId, String sort) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getOrigin() != Origin.SYSTEM || quiz.getType() != QuizType.WEEKEND) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOT_WEEKEND_QUIZ");
        }

        WeekendQuizVoteResultDto voteResult = getVoteResult(quizId, currentUserId);
        List<CommentDto> comments = getCommentsWithTimeFormat(quizId, currentUserId, sort);

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

    // 댓글 목록 조회
    private List<CommentDto> getCommentsWithTimeFormat(Long quizId, Long currentUserId, String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            return commentRepository.findCommentsByQuizIdOrderByPopular(quizId, currentUserId);
        }
        return commentRepository.findCommentsByQuizIdOrderByLatest(quizId, currentUserId);
    }

    // 퀴즈 좋아요
    @Transactional
    public void selectQuizLike(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getOrigin() == Origin.SYSTEM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_LIKE_SYSTEM_QUIZ");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        var existingLike = quizLikeRepository.findByQuiz_QuizIdAndUser_UserId(quizId, userId);
        if (existingLike.isPresent()) {
            quizLikeRepository.delete(existingLike.get());
        } else {
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
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getOrigin() == Origin.SYSTEM && quiz.getType() == QuizType.WEEKDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_COMMENT_ON_SYSTEM_QUIZ");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        Comment comment = new Comment();
        comment.setQuiz(quiz);
        comment.setUser(user);
        comment.setContent(commentCreateDto.getContent());
        comment.setWriterAnonymous(commentCreateDto.getIsAnonymous() != null && commentCreateDto.getIsAnonymous());
        comment.setStatus(Status.DONE);
        comment.setCommentAnonymous(CommentAnonymous.OPEN);
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setModifiedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        if (quiz.getUserId() != null && !quiz.getUserId().equals(userId)) {
            notificationService.sendNotification(
                    quiz.getUserId(),
                    "COMMENT",
                    user.getNickname() + "님이 회원님의 게시물에 댓글을 남겼어요!"
            );
        }

        return savedComment.getCommentId();
    }

    // 댓글 좋아요 선택
    @Transactional
    public void selectCommentLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        var existingLike = commentLikeRepository.findByComment_CommentIdAndUser_UserId(commentId, userId);
        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
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
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        Boolean isAnonymous = dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false;

        Quiz quiz = Quiz.builder()
                .origin(Origin.USER)
                .type(QuizType.WEEKDAY)
                .content(dto.getContent())
                .category(dto.getCategory())
                .userId(userId)
                .isAnonymous(isAnonymous)
                .publishedDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        System.out.println("=== Quiz Created ===");
        System.out.println("quizId: " + savedQuiz.getQuizId());
        System.out.println("publishedDate: " + savedQuiz.getPublishedDate());
        System.out.println("category: " + savedQuiz.getCategory());
        System.out.println("origin: " + savedQuiz.getOrigin());

        return savedQuiz.getQuizId();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    // 다른 유저의 댓글 신고
    @Transactional
    public void reportComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"));

        boolean alreadyReported = reportUserRepository.existsByReporterIdAndContentTypeAndContentId(
                userId, ContentType.COMMENT, commentId
        );

        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_REPORTED");
        }

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
        if (blockedUserId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_BLOCK_YOURSELF");
        }

        usersRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        boolean alreadyBlocked = blockUserRepository.existsByBlockerIdAndBlockedId(currentUserId, blockedUserId);

        if (alreadyBlocked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_BLOCKED");
        }

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

        if (!quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        Long commentCount = quizRepository.countCommentsByQuizId(quizId);
        if (commentCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_EDIT_QUIZ_WITH_COMMENTS");
        }

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

        if (!quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        quizRepository.delete(quiz);
    }

    // 게시물 신고
    @Transactional
    public void reportQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QUIZ_NOT_FOUND"));

        if (quiz.getUserId() != null && quiz.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_REPORT_OWN_QUIZ");
        }

        boolean alreadyReported = reportUserRepository.existsByReporterIdAndContentTypeAndContentId(
                userId, ContentType.QUESTION, quizId
        );

        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY_REPORTED");
        }

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