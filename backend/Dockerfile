# ==============================================================================
# Multi-stage Dockerfile for Spring Boot Backend on Render (Java 21)
# ==============================================================================

# Stage 1: Build stage with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Pre-fetch Maven dependencies to optimize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build the Spring Boot application artifact
RUN mvn clean package -DskipTests -B

# Locate and extract the repackaged executable Spring Boot JAR
RUN find target -maxdepth 1 -name "campus-connect-backend-*.jar" ! -name "*.original" -exec cp {} /build/app.jar \;

# Stage 2: Production runtime stage with Java 21 JRE
FROM eclipse-temurin:21-jre-jammy AS runtime

# Set working directory
WORKDIR /app

# Create a dedicated non-root user and group for security
RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup --no-create-home appuser

# Pre-create upload directory with proper permissions for the non-root user
RUN mkdir -p /app/uploads/resources && \
    chown -R appuser:appgroup /app

# Copy the executable JAR from the build stage with appropriate ownership
COPY --from=build --chown=appuser:appgroup /build/app.jar /app/app.jar

# Switch to the non-root user
USER appuser:appgroup

# Render dynamically injects the PORT environment variable (default fallback: 8080)
ENV PORT=8080
EXPOSE 8080

# Configure JVM execution to support Render's dynamic PORT binding and graceful shutdown
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
