# --------------------------
# Stage 1: Build the application
# --------------------------
    FROM eclipse-temurin:17-jdk AS builder

    WORKDIR /app
    
    # Copy project files
    COPY . .
    
    # Fix permissions for Maven wrapper
    RUN chmod +x mvnw
    
    # Build the JAR and run tests
    RUN ./mvnw clean verify
    
    # --------------------------
    # Stage 2: Runtime
    # --------------------------
    FROM eclipse-temurin:17-jdk
    
    WORKDIR /app
    
    # Copy the built JAR
    COPY --from=builder /app/target/*.jar app.jar
    
    # Copy wait-for-it script
    COPY wait-for-it.sh /wait-for-it.sh
    RUN chmod +x /wait-for-it.sh
    
    EXPOSE 8083
    
    # --------------------------
    # EntryPoint: wait-for-it optional
    # --------------------------
    # Behavior:
    #   - If WAIT_FOR_IT=true => wait for WAIT_FOR_IT_HOSTS (comma-separated)
    #   - Else => just run the JAR immediately
    
    ENTRYPOINT ["/bin/sh", "-c", "\
      if [ \"$WAIT_FOR_IT\" = \"true\" ]; then \
        for host in $(echo $WAIT_FOR_IT_HOSTS | tr ',' ' '); do \
          /wait-for-it.sh $host; \
        done; \
      fi; \
      exec java -jar app.jar \
    "]
    