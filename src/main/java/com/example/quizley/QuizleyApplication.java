package com.example.quizley;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class QuizleyApplication {
	public static void main(String[] args) {
		SpringApplication.run(QuizleyApplication.class, args);
	}
}
