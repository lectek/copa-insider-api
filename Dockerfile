# syntax=docker/dockerfile:1.5

FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY src src

RUN mvn -B -ntp -DskipTests=true -DskipITs=true package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-lc", "exec java $JAVA_OPTS -jar /app/app.jar"]
