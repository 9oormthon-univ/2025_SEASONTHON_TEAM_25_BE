# ---- build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
RUN apk add --no-cache bash curl unzip
COPY . .
RUN chmod +x ./gradlew && ./gradlew :main-server:bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/main-server/build/libs/main-server.jar /app/main-server.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTS -jar /app/main-server.jar"]
