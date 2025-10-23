package com.example.quizley.controller;

import com.example.quizley.common.ApiSuccess;
import com.example.quizley.config.jwt.JwtProvider;
import com.example.quizley.dto.users.LoginFormDto;
import com.example.quizley.dto.users.SignupFormDto;
import com.example.quizley.entity.users.Users;
import com.example.quizley.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.Map;


// 회원가입, 로그인 로직
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupFormDto signupFormDto) {
        // 비밀번호 해시 + 저장
        Users user = Users.createUser(signupFormDto, passwordEncoder);
        usersService.saveUser(user);

        // 컨벤션: 데이터 없을 때 status + message
        return ResponseEntity.status(201).body(new ApiSuccess(201, "성공적으로 처리되었습니다."));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginFormDto loginFormDto) {

        UserDetails user;

        // id로 유저 조회
        try {
            user = usersService.loadUserByUsername(loginFormDto.getUserId());
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // 입력된 비번과 저장된 해시 비교
        if (!passwordEncoder.matches(loginFormDto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // JWT 발급
        String accessToken  = jwtProvider.createAccessToken(user.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        // refresh 토큰 DB에 저장
        usersService.updateRefreshToken(user.getUsername(), refreshToken);

        // JSON 응답 데이터
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "로그인 성공");
        body.put("accessToken", accessToken);
        body.put("refreshToken", refreshToken);

        // JSON 응답 생성 (accessToken 발급)
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(body);

    }

    // Access + Refresh Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        // 프론트가 보낸 refreshToken 값 가져오기
        String refreshToken = request.get("refreshToken");

        // refreshToken 누락 체크
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_REQUIRED");
        }

        // refreshToken 내부에서 userId 추출
        String userId = jwtProvider.getSubject(refreshToken);

        // DB에 저장된 refreshToken과 일치하는지 검증
        if (!usersService.validateRefreshToken(userId, refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }

        // 새 Access Token + 새 Refresh Token 발급
        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        // DB에 새 refreshToken 갱신 (기존 것은 무효화)
        usersService.updateRefreshToken(userId, newRefreshToken);

        // 응답 JSON
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("message", "토큰 재발급 완료");
        body.put("accessToken", newAccessToken);
        body.put("refreshToken", newRefreshToken);

        return ResponseEntity.ok(body);
    }
}
