# Use an openjdk image for runtime (using a specific version tag)
FROM openjdk:17-jdk-slim
# Set working directory in the container
WORKDIR /app
# Copy the built JAR file from Jenkins (assuming it's in the workspace)
COPY target/onboarding-app-backend-*.jar /app/onboarding-app-backend.jar
# Expose the application port
EXPOSE 8084
# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "onboarding-app-backend.jar"]
