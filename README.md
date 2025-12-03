## 📁 프로젝트 구조
```
Quizley
├── .github
│   ├── ISSUE_TEMPLATE
│   │   └── feature_request.md       # 기능 요청 이슈 템플릿
│   ├── pull_request_template.md     # PR 템플릿
│   └── workflows
│       └── deploy.yml               # CI/CD 배포 워크플로우
├── .gradle / .idea / build / gradle # Gradle 및 IDE 설정/빌드 산출물
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.quizley
│   │   │       ├── common
│   │   │       │   └── level        # 레벨업 도메인/서비스 (LevelService 등)
│   │   │       ├── config
│   │   │       │   ├── claude       # Anthropic Claude 연동 설정 및 프롬프트 로더
│   │   │       │   └── jwt          # Spring Security + JWT 인증/인가 설정
│   │   │       ├── controller       # REST API 컨트롤러 (AdminQuiz, Calendar, User 등)
│   │   │       ├── domain           # 공통 도메인 상수/Enum (Category, QuizType 등)
│   │   │       ├── dto              # 계층 간 데이터 전달용 DTO
│   │   │       ├── entity           # JPA 엔티티 (users, quiz, comment, notification 등)
│   │   │       ├── repository       # Spring Data JPA 레포지토리
│   │   │       ├── service          # 비즈니스 로직 레이어
│   │   │       ├── storage          # AWS S3 파일 업로드/다운로드 (AwsS3Config, S3Service 등)
│   │   │       ├── util             # 공통 유틸 (TimeFormatUtil 등)
│   │   │       └── QuizleyApplication.java # Spring Boot 진입점
│   │   └── resources
│   │       ├── prompts.weekday          # 요일별 AI 프롬프트 템플릿 (quiz, chat, summary 등)
│   │       └── application.properties   # DB/JPA/외부 API 등 애플리케이션 설정
│   └── test                             # 테스트 코드
├── .env                                 # 로컬 환경 변수 (DB, API 키 등)
└── 기타 Gradle 설정 파일들 (build.gradle, settings.gradle 등)
```



## 🛠 Tech Stack

- Language
  - Java 17

- Backend
  - Spring Boot
  - Spring Web
  - Spring Security + JWT
  - Spring Data JPA

- Database
  - MySQL (AWS RDS)

- Infrastructure / DevOps
  - AWS EC2 (애플리케이션 서버)
  - AWS RDS (MySQL DB)
  - AWS S3 (이미지/파일 스토리지)
  - GitHub Actions (deploy.yml 기반 CI/CD 파이프라인)
  - Nginx (리버스 프록시, HTTP/HTTPS, SSL 종료)  

- AI / External API
  - Anthropic Claude API (퀴즈/요약/챗봇 기능)

- Build & IDE
  - Gradle


## 🚀 Deployment

### 인프라 구조

- AWS EC2  
  - Spring Boot 애플리케이션이 실행되는 서버
- AWS RDS (MySQL)  
  - 운영 DB 서버
- AWS S3  
  - 이미지/파일 업로드 및 정적 리소스 저장소

### 배포 방식

- GitHub Actions 기반 CI/CD 사용  
  - `.github/workflows/deploy.yml` 에 배포 파이프라인 정의
  - main에 코드가 머지되면 워크플로우 자동 실행
  - Gradle로 빌드 및 테스트 수행
  - 빌드 결과물(JAR)을 EC2 서버에 배포 후 애플리케이션 재시작

### 배포 플로우

1. 기능 개발 후 브랜치에서 작업
2. GitHub에 Pull Request 생성
3. 코드 리뷰 후 `dev` 브랜치에 머지
4. main, 배포 설정과 확인 후 팀장이 `main` 브랜치에 머지
5. 머지 시 GitHub Actions(`deploy.yml`)가 자동으로 실행
6. EC2에 새로운 버전이 배포되고, 애플리케이션 재시작
7. Nginx가 80/443 포트로 들어오는 요청을 Spring Boot 애플리케이션으로 전달  
8. 애플리케이션은 AWS RDS(MySQL) 및 S3와 연결된 상태로 서비스 제공


