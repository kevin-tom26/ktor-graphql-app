# 🛠️ Stage 1: Build the application inside Docker
FROM gradle:8.4-jdk21 AS build
WORKDIR /app

# Copy Gradle files first for caching dependencies
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Give execution permission to Gradle wrapper
RUN chmod +x gradlew

# Download dependencies (before copying source code to improve caching)
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the project files
COPY . .

# Build the application
RUN ./gradlew clean shadowJar --no-daemon

# 🌟 Stage 2: Create a lightweight runtime container
FROM amazoncorretto:21
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port (change if needed)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
