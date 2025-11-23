package com.example.quizley.entity.quiz;

import lombok.*;
import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuizLikeId implements Serializable {
    private Long quiz;  //Quiz 엔티티의 PK타입
    private Long user;  //Users 엔티티의 PK타입
}
