package com.example.quizley.config.claude;


// txt 파일 제목 ENUM
public enum WeekdayPromptType {
    QUIZ("quiz"),
    CHAT("chat"),
    SUMMARY("summary"),
    BOT_SUMMARY("bot_summary"),
    FEEDBACK("feedback"),;

    private final String fileName;

    WeekdayPromptType(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}
