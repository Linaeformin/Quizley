package com.example.quizley.repository;

import com.example.quizley.entity.quiz.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface ReportRepository extends JpaRepository<AiChat, Long> {

    // (이미 있던) 유저별 카테고리별 참여 횟수
    @Query("SELECT c.quiz.category AS category, COUNT(c) AS cnt " +
            "FROM AiChat c WHERE c.users.userId = :userId GROUP BY c.quiz.category")
    List<Object[]> findCategoryStats(Long userId);

    default Map<String, Integer> countByCategoryForUser(Long userId) {
        return findCategoryStats(userId).stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
    }

    // 각 유저의 가장 최근에 끝난 연속 답변 일자를 current streak으로 리턴
    @Query(value = """
        WITH dates AS (
          SELECT c.user_id, DATE(c.created_at) d
          FROM comment c
          GROUP BY c.user_id, DATE(c.created_at)
        ),
        seq AS (
          SELECT user_id, d,
                 LAG(d) OVER (PARTITION BY user_id ORDER BY d) AS prev_d
          FROM dates
        ),
        grp AS (
          SELECT user_id, d,
                 CASE WHEN prev_d = DATE_SUB(d, INTERVAL 1 DAY) THEN 0 ELSE 1 END AS is_break
          FROM seq
        ),
        grp2 AS (
          SELECT user_id, d,
                 SUM(is_break) OVER (PARTITION BY user_id ORDER BY d) AS g
          FROM grp
        ),
        streaks AS (
          SELECT user_id, g,
                 COUNT(*) AS len,
                 MAX(d)  AS end_d
          FROM grp2
          GROUP BY user_id, g
        ),
        current AS (
          SELECT s.user_id, s.len
          FROM streaks s
          JOIN (
            SELECT user_id, MAX(end_d) AS last_d
            FROM streaks
            GROUP BY user_id
          ) t
            ON s.user_id = t.user_id AND s.end_d = t.last_d
        )
        SELECT user_id, len FROM current
        """, nativeQuery = true)
    List<Object[]> findCurrentStreakAllUsers();
}

