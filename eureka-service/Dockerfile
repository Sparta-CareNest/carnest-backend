# eureka-service/Dockerfile
FROM openjdk:17-jdk AS builder
WORKDIR /app
COPY . /app

RUN ./gradlew bootJar -x test

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]