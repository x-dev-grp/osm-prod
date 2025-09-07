# syntax=docker/dockerfile:1.4   # enable BuildKit secrets & cache

############################
# ⬆️ 1) BUILD STAGE (Maven)
############################
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Pre-fetch deps for better cache hits
COPY pom.xml .
RUN --mount=type=secret,id=maven_settings,dst=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2 \
    mvn -B -s /root/.m2/settings.xml -DskipTests dependency:go-offline

# 2) Build the app
COPY src ./src
RUN --mount=type=secret,id=maven_settings,dst=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2 \
    mvn -B -s /root/.m2/settings.xml -DskipTests clean package

############################
# ⬇️ 2) RUNTIME STAGE (JRE)
############################
FROM eclipse-temurin:21-jre
WORKDIR /app

# run as non-root (good practice)
RUN useradd -r -u 10001 -g root appuser

# copy the fat JAR produced in the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# service port for productionservice
ARG SERVICE_PORT=8083
ENV SERVER_PORT=${SERVICE_PORT}

# sensible defaults for containers
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE ${SERVICE_PORT}

USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
