FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /chat
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DiskipTests

FROM openjdk:17-jdk-slim
WORKDIR /chat
COPY --from=build /chat/target/*.jar chat.jar

ENV EUREKA_URI=http://locahost:8781/eureka
ENV RABBIT_HOST=locahost

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "chat.jar" ]