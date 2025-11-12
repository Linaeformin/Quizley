package com.example.quizley.service;

import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


// 회원가입, 로그인 서비스
@Service
public class UsersService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // 회원가입
    public Users saveUser(Users user) {
        validateDuplicateId(user);
        return usersRepository.save(user);
    }

    // userId 중복 확인
    private void validateDuplicateId(Users user) {
        if (usersRepository.existsById(user.getId())) {
            throw new ResponseStatusException(CONFLICT, "DUPLICATE_USER_ID");
        }
    }

    // refresh token 저장
    @Transactional
    public void updateRefreshToken(String userId, String refreshToken) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS"));
        user.setRefreshToken(refreshToken);
        usersRepository.save(user);
    }

    // refresh token 검증
    public boolean validateRefreshToken(String userId, String refreshToken) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS"));
        return refreshToken.equals(user.getRefreshToken());
    }

    // 로그인 시 호출 (id로 유저 조회)
    @Override
    public UserDetails loadUserByUsername(String id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "INVALID_CREDENTIALS"));

        // CustomUserDetails 로 래핑해서 반환
        return new CustomUserDetails(
                user.getUserId(),    // PK
                user.getId(),                   // 로그인용 id
                user.getPassword(),             // 비밀번호
                Collections.emptyList()         // 권한 (필요 시 추가 가능)
        );
    }
}
