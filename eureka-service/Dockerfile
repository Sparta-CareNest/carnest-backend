# eureka-service/Dockerfile
FROM gradle:8.2.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]