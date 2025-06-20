# --------------------------
# Stage 1: Build
# --------------------------
    FROM eclipse-temurin:17-jdk AS builder

    WORKDIR /app
    
    # Copy project files
    COPY . .
    
    # --------------------------
    # Declare build arguments
    # --------------------------
    ARG SPRING_CLOUD_CONFIG_SERVER_GIT_URI
    ARG SPRING_CLOUD_CONFIG_SERVER_GIT_SEARCH_PATHS
    ARG SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL
    
    # Provide as environment variables for Maven execution
    ENV SPRING_CLOUD_CONFIG_SERVER_GIT_URI=$SPRING_CLOUD_CONFIG_SERVER_GIT_URI
    ENV SPRING_CLOUD_CONFIG_SERVER_GIT_SEARCH_PATHS=$SPRING_CLOUD_CONFIG_SERVER_GIT_SEARCH_PATHS
    ENV SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL=$SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL
    
    
    # Fix permissions for wrapper
    RUN chmod +x mvnw
    
    # Run build & tests
    RUN ./mvnw clean verify
    
    # --------------------------
    # Stage 2: Runtime
    # --------------------------
    FROM eclipse-temurin:17-jdk
    
    WORKDIR /app
    
    # Copy the final jar
    COPY --from=builder /app/target/*.jar app.jar
    
    # Expose port
    EXPOSE 8888
    
    # Run the app
    ENTRYPOINT ["java", "-jar", "app.jar"]
    