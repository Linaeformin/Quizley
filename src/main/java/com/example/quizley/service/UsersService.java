package com.example.quizley.service;

import com.example.quizley.entity.users.Users;
import com.example.quizley.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;


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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
