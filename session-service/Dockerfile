# Stage 1: build the application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: run the application
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Copy wait-for-it.sh script
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8082

# Use wait-for-it to wait for config-server on port 8888
CMD ["/wait-for-it.sh", "config-server:8888", "--", "/wait-for-it.sh", "eureka-server:8761", "--", "java", "-jar", "app.jar"]
