# Stage 1: build & test
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x mvnw
# Esegui test + build (non saltare i test!)
RUN ./mvnw clean verify

# Salva i report di test in una cartella separata (optional)
RUN mkdir -p /test-reports && cp -r target/surefire-reports /test-reports/

# Stage 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copia solo il JAR finale
COPY --from=builder /app/target/*.jar app.jar

# Copia anche i report se vuoi accedervi più tardi
COPY --from=builder /test-reports /test-reports

# wait-for-it script
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8084

CMD ["/wait-for-it.sh", "config-server:8888", "--", "/wait-for-it.sh", "eureka-server:8761", "--", "java", "-jar", "app.jar"]
