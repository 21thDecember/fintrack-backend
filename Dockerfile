# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/target/*.jar app.jar

ENV PORT=8085
EXPOSE 8085

# Render automatically sets PORT. Spring Boot reads ${PORT:8085}
# Memory optimization for Render Free Tier (512MB RAM)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Xss512k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
