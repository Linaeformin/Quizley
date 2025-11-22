package com.example.quizley.dto.profile;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponseDto {

    private String nickname;
    private String profile;
    private Integer level;

    private Integer currentExp;   // point = 경험치
    private Integer requiredExp;  // 다음 레벨까지 필요한 경험치
}
