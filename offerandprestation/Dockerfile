# Étape 1 : Compilation
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Étape 2 : Image finale
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
VOLUME /app/uploads-spotlight
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]