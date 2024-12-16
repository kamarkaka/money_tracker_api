# Use an official Maven image to build the app
FROM maven:3.9.9-eclipse-temurin-22-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY java ./java

# Build the application
# environment variables required:
# MONEY_TRACKER.API.PORT
# MONEY_TRACKER.DB.HOST
# MONEY_TRACKER.DB.PORT
# MONEY_TRACKER.DB.NAME
# MONEY_TRACKER.DB.USER
# MONEY_TRACKER.DB.PASSWORD
RUN echo "db: ${MONEY_TRACKER.DB.HOST}:${MONEY_TRACKER.DB.PORT}/${MONEY_TRACKER.DB.NAME}"
RUN echo "db user: ${MONEY_TRACKER.DB.USER}"
RUN echo "api port: ${MONEY_TRACKER.API.PORT}"
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
EXPOSE ${MONEY_TRACKER.API.PORT}

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/money-tracker.jar", "server", "/app/config.yml"]