package com.example.quizley.repository;

import com.example.quizley.entity.quiz.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


// 채팅방 레포지토리
public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    // user.userId와 quiz.quizId로 존재 여부
    boolean existsByQuiz_QuizIdAndUsers_UserId(Long quizId, Long userId);

    // 해당 조합으로 채팅방 1건 조회
    Optional<AiChat> findByQuiz_QuizIdAndUsers_UserId(Long quizId, Long userId);

    // 성능 최적화: chatId만 필요할 때 (옵션)
    @Query("select c.chatId from AiChat c where c.quiz.quizId = :quizId and c.users.userId = :userId")
    Optional<Long> findChatIdByQuizIdAndUserId(@Param("quizId") Long quizId, @Param("userId") Long userId);

    // chatId와 유저 Id로 조회
    Optional<AiChat> findByChatIdAndUsers_UserId(Long chatId, Long userId);
}
