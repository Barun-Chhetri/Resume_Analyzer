# ==========================================
# Stage 1: Build React Frontend (Vite SPA)
# ==========================================
FROM node:22-slim AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package.json ./
RUN npm install && npm install --save-optional @rollup/rollup-linux-x64-gnu

COPY frontend/ ./
RUN npm run build

# ==========================================
# Stage 2: Build Spring Boot Application JAR
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /app

COPY backend/pom.xml ./
COPY backend/mvnw ./
COPY backend/.mvn .mvn/

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

COPY backend/src ./src

# Embed compiled React frontend assets into Spring Boot's static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Stage 3: Minimal Production JRE Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=backend-builder /app/target/resume-api-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
