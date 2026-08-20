#!/usr/bin/env bash
# 扫描当前后端工程到 SonarQube
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SONAR_HOST_URL="${SONAR_HOST_URL:-http://192.168.3.4:19000}"
SONAR_TOKEN="${SONAR_TOKEN:-squ_153105557ce4791378968772dc2bd2292d4436ff}"

echo "==> Sonar 扫描 host=$SONAR_HOST_URL"
cd "$ROOT"
mvn spring-javaformat:apply spotless:apply &&
  mvn clean compile test sonar:sonar \
    -Dsonar.host.url="$SONAR_HOST_URL" \
    -Dsonar.login="$SONAR_TOKEN" \
    -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

echo "Sonar 扫描完成"
