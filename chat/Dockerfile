# Étape 1 : Build (Compilation avec Maven)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# On package l'app en sautant les tests pour gagner du temps au build docker
RUN mvn clean package -DskipTests

# Étape 2 : Run (Image finale légère)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# On récupère le .jar généré à l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# Configuration des variables d'environnement par défaut 
ENV EUREKA_URI=http://registry-service:8761/eureka
ENV RABBIT_HOST=rabbitmq

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]