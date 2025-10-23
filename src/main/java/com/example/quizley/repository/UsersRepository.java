package com.example.quizley.repository;

import com.example.quizley.entity.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


// 유저 레포지토리
public interface UsersRepository extends JpaRepository<Users, Long> {
    // 유저 아이디 중복 검사
    boolean existsById(String id);

    // 유저 아이디로 해당 유저 찾기
    Optional<Users> findById(String id);
}
