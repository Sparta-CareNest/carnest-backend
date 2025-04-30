# 1. JDK 17 버전 이미지 가져오기
FROM openjdk:17-jdk

# 2. 빌드한 jar 파일을 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너가 실행될 때 jar 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]