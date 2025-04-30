FROM openjdk:17

WORKDIR /app

COPY ./config-service/build/libs/config-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]