# Étape 1 : Compilation (Build)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# On télécharge les dépendances pour qu'elles soient en cache
RUN ./mvnw dependency:go-offline
COPY src ./src
# On compile le projet sans lancer les tests pour aller plus vite
RUN ./mvnw clean package -DskipTests

# Étape 2 : Création de l'image finale (Run)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# On récupère le .jar généré à l'étape précédente
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]