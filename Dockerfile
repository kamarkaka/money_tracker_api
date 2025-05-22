# Use an official Maven image to build the app
FROM maven:3.9.9-eclipse-temurin-22-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY java ./java

RUN mvn clean package

# Use a smaller image for the final application
FROM eclipse-temurin:22-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/money-tracker-1.0.0.jar /app/money-tracker.jar

# Expose port for the application
EXPOSE ${API_PORT}

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/money-tracker.jar", "server", "/app/config.yml"]
