package com.example.quizley.entity.users;

import com.example.quizley.dto.users.SignupFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;


// 유저 엔티티
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"}) // 로그인 ID 중복 방지
})
@Getter @Setter
@ToString
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId; // PK

    @Column(name = "id", nullable = false, length = 15, unique = true)
    private String id; // 사용자가 입력한 ID

    @Column(nullable = false, length = 100)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false, length = 10)
    private String nickname; // 닉네임

    @Column(name = "profile")
    private String profile; // 프로필 이미지 경로

    @Column(nullable = false)
    private Integer point = 0; // 보유 포인트 (기본값 0)

    @Column(nullable = false)
    private Integer level; // 레벨

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 회원가입 일자

    @UpdateTimestamp
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt; // 프로필 수정 일자

    // 회원가입 시 User 생성
    public static Users createUser(SignupFormDto signupFormDto, PasswordEncoder passwordEncoder) {
        Users user = new Users();
        user.setId(signupFormDto.getUserId());
        user.setPassword(passwordEncoder.encode(signupFormDto.getPassword()));
        user.setNickname(signupFormDto.getNickname());
        user.setPoint(0);
        user.setLevel(1);
        return user;
    }
}
