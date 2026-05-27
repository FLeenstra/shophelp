# --- Build stage: compile and package with Maven (no local JDK/Maven needed) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage: slim JRE image ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/shophelp-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
