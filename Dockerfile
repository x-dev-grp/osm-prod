# syntax=docker/dockerfile:1.4

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 -g root appuser

COPY --from=build /app/target/*.jar /app/app.jar
ARG SERVICE_PORT=8083
ENV SERVER_PORT=${SERVICE_PORT}
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE ${SERVICE_PORT}
USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
