package com.example.quizley.controller;

import com.example.quizley.config.claude.ClaudeGateway;
import com.example.quizley.config.claude.PromptLoader;
import com.example.quizley.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


// 로컬 환경에서 질문 생성 테스트
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/quiz")
public class AdminQuizController {

    private final PromptLoader prompts;
    private final ClaudeGateway claude;
    private final QuizService quizService;

    // 질문 생성
    @PostMapping("/generate")
    public Map<String,Object> generate(@RequestParam(defaultValue="weekday") String tag){

        // 프롬프트 로드
        String prompt = prompts.load(tag);

        // 질문 생성
        Map<String,String> map = claude.generateMapFromPrompt(prompt);

        // 질문 저장
        int saved = quizService.saveSystemWeekdayBulk(map);
        return Map.of("saved", saved, "keys", map.keySet());
    }
}

