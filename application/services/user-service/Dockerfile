
# ============================================================
# ShopSphere - User Service
# Dockerfile
# ECOM-010 Release Pipeline
# ============================================================

# Use lightweight Java 21 runtime
FROM eclipse-temurin:21-jre

# Application working directory
WORKDIR /app

# Create non-root application user
RUN useradd --system \
    --create-home \
    --shell /usr/sbin/nologin \
    appuser

# Copy the validated Maven artifact.
#
# ECOM-010 Jenkins Release Pipeline downloads the exact
# validated JAR from Nexus and places it under:
#
# target/user-service.jar
#
# Keeping COPY target/*.jar preserves the existing
# ShopSphere Docker build contract.
COPY target/*.jar app.jar

# Give application user ownership
RUN chown -R appuser:appuser /app

# Run container as non-root user
USER appuser

# Spring Boot application port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
