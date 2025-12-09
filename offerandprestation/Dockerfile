FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

VOLUME /app/uploads-spotlight

COPY target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]