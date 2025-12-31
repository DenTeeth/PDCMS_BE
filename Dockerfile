# ===========================================
# Stage 1: Build with Maven (JDK 17)
# ===========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .

# Pre-download dependencies to cache them
RUN mvn dependency:go-offline -B

# Copy source code into container
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# ===========================================
# Stage 2: Runtime (JRE 17 Alpine)
# ===========================================
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Install dumb-init and timezone data for proper signal handling and timezone support
RUN apk add --no-cache dumb-init tzdata

# Set timezone to Asia/Ho_Chi_Minh (Vietnam)
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create a non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 && \
    chown -R spring:spring /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER spring

# Expose the application port
EXPOSE 8080

# JVM options (production-ready)
# Added user.timezone to ensure JVM uses correct timezone for scheduled tasks
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Duser.timezone=Asia/Ho_Chi_Minh"

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the Spring Boot application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
