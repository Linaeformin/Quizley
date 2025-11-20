package com.example.quizley.entity.users;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BlockUserId implements Serializable {
    private Long blockerId;
    private Long blockedId;
}
