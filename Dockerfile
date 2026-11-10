# Stage 1: Build the application using Java 26 JDK
FROM eclipse-temurin:26-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give execution permission to mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the jar
COPY src src
RUN ./mvnw package -DskipTests

# Stage 2: Run the application using Java 26 JRE
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default port (Render overrides PORT at runtime)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]