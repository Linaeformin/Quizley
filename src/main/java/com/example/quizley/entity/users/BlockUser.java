package com.example.quizley.entity.users;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BlockUserId.class)
public class BlockUser {

    @Id
    @Column(name = "blocker_id", nullable = false)
    private Long blockerId; // 차단을 건 유저 (PK)

    @Id
    @Column(name = "blocked_id", nullable = false)
    private Long blockedId; // 차단당한 유저 (PK)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}