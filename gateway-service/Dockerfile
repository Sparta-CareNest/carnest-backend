FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# 빌드 캐시를 위해 Gradle 파일 먼저 복사
COPY gateway-service/build.gradle gateway-service/settings.gradle ./
COPY gateway-service/gradle ./gradle

# 의존성 다운로드 (캐싱 활용)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY gateway-service/src ./src

# 애플리케이션 빌드
RUN gradle build -x test --no-daemon

# 런타임 스테이지
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
