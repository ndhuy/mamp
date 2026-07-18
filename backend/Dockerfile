# --- Build stage: compile the Spring Boot jar ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Cache dependencies first for faster rebuilds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage: slim JRE image ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Respect container memory limits
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
