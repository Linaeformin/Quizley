package com.example.quizley.entity.quiz;

import com.example.quizley.domain.ChatStatus;
import com.example.quizley.entity.users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// AI 채팅방 엔티티
@Entity
@Table(name = "ai_chat")
@Getter @Setter
public class AiChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false, updatable = false)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_aichat_user"))
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_aichat_quiz"))
    private Quiz quiz;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    private String summary;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "chat_status", nullable = false)
    private ChatStatus chatStatus = ChatStatus.OPEN;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiMessage> messages = new ArrayList<>();

    // 편의 메서드
    public void addMessage(AiMessage m) {
        messages.add(m);
        m.setChat(this);
    }

    public void close() {
        this.chatStatus = ChatStatus.CLOSED;
    }

    public void open() {
        this.chatStatus = ChatStatus.OPEN;
    }
}