package com.example.quizley.controller;

import com.example.quizley.common.ApiSuccess;
import com.example.quizley.dto.users.SignupFormDto;
import com.example.quizley.entity.users.Users;
import com.example.quizley.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// 회원가입, 로그인 로직
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupFormDto signupFormDto) {
        // 비밀번호 해시 + 저장
        Users user = Users.createUser(signupFormDto, passwordEncoder);
        usersService.saveUser(user);

        // 컨벤션: 데이터 없을 때 status + message
        return ResponseEntity.status(201).body(new ApiSuccess(201, "성공적으로 처리되었습니다."));
    }
}
