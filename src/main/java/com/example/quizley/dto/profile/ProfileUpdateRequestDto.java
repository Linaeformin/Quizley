package com.example.quizley.dto.profile;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileUpdateRequestDto {
    private String nickname;
    private String password;
    private MultipartFile profileImage;
}
