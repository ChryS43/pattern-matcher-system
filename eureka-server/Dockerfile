# --------------------------
# Stage 1: Build
# --------------------------
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

# --------------------------
# Stage 2: Runtime
# --------------------------
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8761

# Conditional wait-for-it
ENTRYPOINT ["/bin/sh", "-c", "if [ \"$WAIT_FOR_IT\" = \"true\" ]; then /wait-for-it.sh config-server:8888 -- java -jar app.jar; else java -jar app.jar; fi"]
