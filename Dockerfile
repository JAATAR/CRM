FROM maven:latest AS build

WORKDIR /app

COPY . .

RUN mvn package

FROM openjdk:21-jdk

WORKDIR /app

# Copy the JAR file built in the previous stage
COPY --from=build /app/target/CRM_Groep1-1.0-SNAPSHOT.jar .

# Command to run your application when the container starts
CMD ["java", "-jar", "CRM_Groep1-1.0-SNAPSHOT.jar"]