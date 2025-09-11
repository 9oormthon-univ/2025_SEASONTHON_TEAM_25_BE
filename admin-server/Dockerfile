# ---- build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
RUN apk add --no-cache bash curl unzip
COPY . .
RUN chmod +x ./gradlew && ./gradlew :admin-server:bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/admin-server/build/libs/admin-server.jar /app/admin-server.jar
EXPOSE 8081
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0"
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTS -jar /app/admin-server.jar"]
