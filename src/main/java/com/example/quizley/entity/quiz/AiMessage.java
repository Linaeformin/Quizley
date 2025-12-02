package com.example.quizley.entity.quiz;

import com.example.quizley.domain.MessageOrigin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


// AI 메시지 엔티티
@Entity
@Table(name = "ai_message")
@Getter @Setter
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false, updatable = false)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_aim_chat"))
    private AiChat chat;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, columnDefinition = "ENUM('AI','USER')")
    private MessageOrigin origin;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime modifiedAt;
}