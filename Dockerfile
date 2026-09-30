# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp verify

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
RUN groupadd --system --gid 10001 ehs && useradd --system --uid 10001 --gid ehs --no-create-home ehs
COPY --from=build --chown=ehs:ehs /build/target/ehspro-api-1.0.0.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=mysql,render \
    PORT=10000 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+ExitOnOutOfMemoryError"
USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
