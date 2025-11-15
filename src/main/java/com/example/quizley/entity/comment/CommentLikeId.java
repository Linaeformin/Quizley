package com.example.quizley.entity.comment;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentLikeId implements Serializable {
    private Long comment; // Comment 엔티티의 PK 타입
    private Long user; // Users 엔티티의 PK 타입
}
