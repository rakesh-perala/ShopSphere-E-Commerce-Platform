FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY target/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
