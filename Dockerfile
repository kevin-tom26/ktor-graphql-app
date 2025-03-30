# 🛠️ Stage 1: Build the application inside Docker
FROM gradle:8.4-jdk21 AS build
WORKDIR /app

# Copy all project files into the container
COPY . .

# Build the project using Gradlegit status
RUN gradle clean shadowJar --no-daemon

# Stage 2: Create the runtime container
FROM amazoncorretto:21
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port defined in Application.conf
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "app.jar"]

# 🌟 Stage 2: Create a lightweight runtime container
FROM amazoncorretto:21
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port (change if needed)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
