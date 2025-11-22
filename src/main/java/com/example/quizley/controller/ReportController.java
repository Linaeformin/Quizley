package com.example.quizley.controller;
import com.example.quizley.config.CustomUserDetails;
import com.example.quizley.dto.report.ReportResponseDto;
import com.example.quizley.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<ReportResponseDto> getReport(
            @AuthenticationPrincipal CustomUserDetails me) {
        // 권한이 있을 때
        if(me == null) return ResponseEntity.status(401).build();

        // 리포트 생성
        ReportResponseDto report = reportService.generateReport(me.getId());
        return ResponseEntity.ok(report);
    }
}