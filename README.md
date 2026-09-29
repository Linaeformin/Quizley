# Quizley 💬

**매일 새로운 질문을 제시하고, AI와의 대화를 통해 생각을 기록하는 서비스**

평일 질문을 생성하고 사용자가 답변과 대화를 남길 수 있습니다. 대화가 길어져도 이전 내용을 이어갈 수 있도록 응답과 한 줄 요약을 저장하고 다음 요청의 맥락으로 사용합니다.

> 팀 프로젝트에서 **백엔드 리드**를 맡아 AI 연동과 질문·대화 기능을 구현하고, PR 리뷰와 배포 절차를 정리했습니다.

## 주요 기능

| 영역 | 내용 |
| --- | --- |
| 오늘의 질문 | 평일 질문 생성 및 게시, 중복 질문 확인 |
| AI 대화 | Claude API를 이용한 질문 기반 대화와 메시지 저장 |
| 대화 맥락 | 이전 메시지와 누적 요약을 다음 대화에 활용, 한 줄 요약 저장 |
| 기록·커뮤니티 | 답변·인사이트·캘린더, 게시글·댓글·좋아요·신고 |
| 계정·운영 | JWT 인증, 프로필, 알림 및 배포 자동화 |

## AI 대화 처리

`ChatService`는 채팅방 소유권을 확인하고, 기존 요약과 이전 메시지를 읽어 Claude API에 전달합니다. 응답을 받은 뒤 사용자·AI 메시지를 저장하고 새 한 줄 요약을 기존 요약에 누적합니다. 다음 대화에서는 이 요약을 다시 맥락으로 사용합니다.

```text
사용자 메시지 → 기존 요약·직전 메시지 조회 → Claude API 호출
             → 응답·새 요약 저장 → 완성된 응답 반환
```

`POST /api/today/{chatId}/messages`는 생성이 완료된 응답과 새 요약을 반환합니다.

평일 질문은 `WeekdayMidnightJob`의 서울 시간 기준 스케줄 작업을 통해 생성합니다. 생성 과정은 `AdminQuizService`와 `QuizService`에서 확인할 수 있습니다.

## 기술 스택

- **Backend:** Java 17, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, QueryDSL
- **Data:** MySQL
- **AI:** Anthropic Claude API
- **파일·배포:** AWS S3, EC2, GitHub Actions, Gradle

## 주요 API

| 기능 | 요청 |
| --- | --- |
| 회원가입·로그인·토큰 갱신 | `POST /api/users/signup`, `POST /api/users/login`, `POST /api/users/refresh` |
| 오늘의 질문 | `GET /api/today` |
| 대화방 생성 | `POST /api/today/chatroom` |
| AI 대화 | `POST /api/today/{chatId}/messages` |
| 대화 요약 조회 | `GET /api/today/{chatId}/summary` |
| 커뮤니티 조회 | `GET /api/community/home` |

요청·응답 필드와 인증 조건은 `controller`, `dto` 및 `config/SecurityConfig.java`를 참고해 주세요.

## 로컬 실행

Java 17, MySQL, Claude API 키 및 파일 저장 설정이 필요합니다. `src/main/resources/application.properties`에서 참조하는 설정값을 로컬 환경에 준비하세요. **API 키와 DB 비밀번호를 README나 커밋에 넣지 마세요.**

```bash
./gradlew bootRun
```

`.github/workflows/deploy.yml`은 `main` 브랜치 푸시 또는 수동 실행 시 JAR을 빌드하고 EC2에 배포하도록 구성되어 있습니다.

## 담당 범위

백엔드 리드로 질문 생성·AI 대화·요약 맥락 처리를 구현하고, API 협업과 PR 리뷰를 진행했습니다. 배포 중 중복 종료 로직을 확인한 뒤 CI/CD 절차도 조정했습니다.
