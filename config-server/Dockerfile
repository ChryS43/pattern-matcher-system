# Stage 1: build the application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy source code and build the JAR
COPY . .

# Fix permissions on the Maven wrapper script
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Stage 2: create a lightweight runtime image
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
