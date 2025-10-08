# ===========================================
# Stage 1: Build with Maven (JDK 21)
# ===========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .

# Pre-download dependencies to cache them
RUN mvn dependency:go-offline -B

# Copy source code into container
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# ===========================================
# Stage 2: Runtime (JRE 21)
# ===========================================
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# JVM options (optional)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
