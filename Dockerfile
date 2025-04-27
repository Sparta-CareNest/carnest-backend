FROM gradle:8.5-jdk17 AS builder
ARG SERVICE_NAME
WORKDIR /app
COPY . .
RUN ./gradlew :${SERVICE_NAME}:bootJar -x test

FROM openjdk:17
WORKDIR /app
ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]