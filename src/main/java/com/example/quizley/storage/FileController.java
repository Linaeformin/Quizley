package com.example.quizley.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final com.example.quizley.storage.S3Service s3Service;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        String url = s3Service.uploadFile(file, "uploads");

        // TODO: 여기서 url을 DB에 저장하거나, 모델에 담아서 화면에 넘기거나 등등
        System.out.println("S3 URL = " + url);

        return "redirect:/"; // 일단 홈으로 리다이렉트
    }
}
