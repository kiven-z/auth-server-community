# CI 用：jar 已在 runner 上编好，此处只打运行时镜像（本地 compose 仍用各服务目录下的 Dockerfile）
FROM eclipse-temurin:17-jre-jammy

ARG APP_JAR
ARG SERVER_PORT=8080

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY ${APP_JAR} /app/app.jar

ENV TZ=UTC \
    SERVER_PORT=${SERVER_PORT}

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=120s \
  CMD curl -sf http://127.0.0.1:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
