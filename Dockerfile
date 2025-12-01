# Étape 1 : construire le jar exécutable (fat jar Spring Boot)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package spring-boot:repackage -DskipTests

# Étape 2 : image légère pour exécuter le jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

