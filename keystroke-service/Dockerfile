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

# Use environment variable to control wait-for-it behavior
# If WAIT_FOR_IT is set to "false", skip waiting and start directly
# Otherwise, wait for config-server and eureka-server
CMD ["sh", "-c", "if [ \"$WAIT_FOR_IT\" = \"false\" ]; then java -jar app.jar; else /wait-for-it.sh config-server:8888 -- /wait-for-it.sh eureka-server:8761 -- java -jar app.jar; fi"]
