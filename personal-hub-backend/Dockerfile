# Giai đoạn 1: Build ứng dụng
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Once time in a Database when initial
# COPY data.sql ./src/main/resources/data.sql

RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/personal-hub-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]