# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install -DskipTests

# Package stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/config-server-0.0.1-SNAPSHOT.jar .
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "config-server-0.0.1-SNAPSHOT.jar"]

