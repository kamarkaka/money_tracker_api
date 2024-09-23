# Use an official Maven image to build the app
FROM maven:3.9.9-eclipse-temurin-22-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY java ./java

# Build the application
# Use ARG to accept the database password during build
ARG API_PORT
ARG DB_HOST
ARG DB_PORT
ARG DB_NAME
ARG DB_USER
ARG DB_PASSWORD

ENV MONEY_TRACKER.DB.HOST=${DB_HOST}
ENV MONEY_TRACKER.DB.PORT=${DB_PORT}
ENV MONEY_TRACKER.DB.NAME=${DB_NAME}
ENV MONEY_TRACKER.DB.USER=${DB_USER}
ENV MONEY_TRACKER.DB.PASSWORD=${DB_PASSWORD}
RUN mvn clean package

# Use a smaller image for the final application
FROM eclipse-temurin:22-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/money-tracker-1.0.0.jar /app/money-tracker.jar

# Copy the config
COPY config/config.yml ./config.yml

# Expose port for the application
EXPOSE ${API_PORT}

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/money-tracker.jar", "server", "/app/config.yml"]