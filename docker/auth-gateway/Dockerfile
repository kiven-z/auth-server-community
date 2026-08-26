FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY commons commons
COPY modules modules
COPY services services
COPY auth-gateway auth-gateway
COPY docker/maven/settings.xml /root/.m2/settings.xml

RUN mvn -s /root/.m2/settings.xml -pl auth-gateway -am package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/auth-gateway/target/auth-gateway-*.jar /app/app.jar

ENV TZ=UTC
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=120s \
  CMD curl -sf http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
