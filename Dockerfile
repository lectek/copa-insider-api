# syntax=docker/dockerfile:1.5

# ── Build ─────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Cache das dependências separado do código — rebuild só quando pom.xml mudar
COPY pom.xml ./
RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src src
RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests -DskipITs package

# ── Runtime ───────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem root
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder /workspace/target/*.jar app.jar

# Railway injeta PORT automaticamente; fallback 8080
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Dfile.encoding=UTF-8 \
               -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
