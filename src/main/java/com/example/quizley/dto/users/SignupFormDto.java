package com.example.quizley.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


// 회원가입 데이터 받아오기
@Getter @Setter
public class SignupFormDto {
    @NotBlank(message = "아이디를 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;
}
