package com.example.quizley.repository;

import com.example.quizley.entity.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;


// 유저 레포지토리
public interface UsersRepository extends JpaRepository<Users, Long> {
    // 유저 아이디 중복 검사
    boolean existsById(String id);
}
