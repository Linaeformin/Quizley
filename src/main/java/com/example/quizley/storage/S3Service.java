package com.example.quizley.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일을 S3에 업로드하고, 접근 가능한 URL을 리턴
     * @param file 업로드할 파일
     * @param dirName S3 내에서 사용할 디렉토리 이름 (ex: "images", "profile" 등)
     */
    public String uploadFile(MultipartFile file, String dirName) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";

        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String key = dirName + "/" + UUID.randomUUID() + ext; // S3에 저장될 경로

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // 버킷이 퍼블릭이면 이 URL로 바로 접근 가능
        String region = "ap-southeast-2";

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }
}
