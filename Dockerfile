# Use a base image with Java 17
FROM openjdk:17-jdk-slim as build
# Set the working directory in the container
WORKDIR /app
# Copy the Maven wrapper (if you use Maven wrapper)
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Install dependencies
RUN ./mvnw dependency:go-offline
# Copy the rest of the application source code
COPY src ./src
# Build the application (optional: you can skip this step if you already have the JAR file)
RUN ./mvnw clean package -DskipTests
# Start from a smaller base image with only JRE for the final image
FROM openjdk:17-jre-slim
# Set the working directory in the container
WORKDIR /app
# Copy the JAR file from the build stage to the final image
COPY --from=build /app/target/onboarding-app-backend-*.jar /app/onboarding-app-backend.jar
# Expose the application port
EXPOSE 1083
# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "onboarding-app-backend.jar"]
