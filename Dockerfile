# syntax=docker/dockerfile:1.4

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENV SERVER_PORT=8083
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
