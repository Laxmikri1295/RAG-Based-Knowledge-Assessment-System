# Book Reader Backend

Spring Boot 3 (Gradle) backend for the BookReader project.

Tech stack:
- Java 21, Spring Boot 3.5.x
- Gradle Wrapper
- Spring Web, Data JPA, Validation, Actuator, Lombok
- PostgreSQL (runtime)
- Profiles: default (DB), local (no DB)

Endpoints:
- GET /api/health → "OK"
- GET /actuator/health → status

Prerequisites:
- JDK 21 installed (JAVA_HOME pointing to JDK 21)
- Optional: Docker (for local PostgreSQL)

Build:
- cd backend
- ./gradlew clean build -x test

Run without DB (local profile):
- cd backend
- ./gradlew bootRun --args='--spring.profiles.active=local'
- Test: curl http://localhost:8080/api/health

Run with PostgreSQL:
- Start Postgres (Docker):
  docker run --name bookreader-postgres -e POSTGRES_DB=bookreader -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
- Set env vars (macOS/zsh):
  export DB_URL=jdbc:postgresql://localhost:5432/bookreader
  export DB_USERNAME=postgres
  export DB_PASSWORD=postgres
- Start app:
  cd backend
  ./gradlew bootRun
- Test:
  curl http://localhost:8080/actuator/health

Configuration:
- src/main/resources/application.yml → default DB config
- src/main/resources/application-local.yml → disables DB/JPA for quick local runs

Next steps (suggested):
- Add Flyway for DB migrations
- Add Spring Security (JWT) for auth
- Domain: Users, Books (file metadata), Pages, ReadingSessions (progress), Quizzes (AI-generated per pages read)
- File uploads: S3 or local storage; text extraction (PDF/EPUB)
- CORS for frontend dev
