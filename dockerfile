# Stage 1: Build the JAR
FROM gradle:8.10.2-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2: Run the JAR
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/payup-1-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV PORT=8080
CMD ["java", "-jar", "app.jar"]