#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# IDE git hooks often run with a minimal PATH (no login shell).
resolve_mvn() {
  if command -v mvn >/dev/null 2>&1; then
    command -v mvn
    return 0
  fi

  local sdkman_dir="${SDKMAN_DIR:-${HOME}/.sdkman}"
  local candidates=()

  if [[ -n "${MAVEN_HOME:-}" ]]; then
    candidates+=("${MAVEN_HOME}/bin/mvn")
  fi
  candidates+=(
    "${sdkman_dir}/candidates/maven/current/bin/mvn"
    "/usr/local/bin/mvn"
    "/usr/bin/mvn"
    "/opt/maven/bin/mvn"
  )

  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done

  echo "mvn not found; install Maven or add it to PATH" >&2
  return 1
}

ensure_java_for_maven() {
  if command -v java >/dev/null 2>&1; then
    return 0
  fi

  local sdkman_dir="${SDKMAN_DIR:-${HOME}/.sdkman}"
  local java_home="${JAVA_HOME:-${sdkman_dir}/candidates/java/current}"

  if [[ -x "${java_home}/bin/java" ]]; then
    export JAVA_HOME="${java_home}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
    return 0
  fi

  echo "java not found; install a JDK or set JAVA_HOME" >&2
  return 1
}

ensure_java_for_maven
MVN="$(resolve_mvn)"
"$MVN" spring-javaformat:apply spotless:apply
