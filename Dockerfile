# Dockerfile
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
# copy pom first to cache dependency resolution
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_SECURITY_USER_NAME=admin
ENV SPRING_SECURITY_USER_PASSWORD=changeMe
LABEL authors="Rajesh Adhikari"
ENTRYPOINT ["java","-jar","/app/app.jar"]
