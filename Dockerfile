# Stage 1: Build stage
FROM maven:latest AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copy the entire local directory to the container
COPY . .

RUN mvn clean package

# Stage 2: Runtime stage
FROM openjdk:22-jdk

WORKDIR /app

# Copy the JAR file built in the previous stage
COPY --from=build /app/target/CRM_Groep1-1.0-SNAPSHOT.jar .

# Command to run your application when the container starts
CMD ["java", "-jar", "CRM_Groep1-1.0-SNAPSHOT.jar"]

