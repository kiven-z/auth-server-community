#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/compose.yml"
COMPOSE_OVERRIDE="${SCRIPT_DIR}/compose.override.yml"
SUPPORTED_ENVS=(dev prod)

show_help() {
  cat <<'EOF'
用法:
  ./dockerctl.sh <env> <action> [extra args...]

说明:
  compose.yml          运行时（无 build）— 本地与服务器同一份
  compose.override.yml 仅本地构建 — build / build-up 时叠加
  <env> 决定 Spring profile（Nacos DataId）；密钥默认 env/service-system.env
  宿主机仅暴露网关 8080

支持环境:
  dev | prod

支持动作:
  pull       拉取镜像（远程仓库，不构建）
  build      本地构建镜像（需源码 + override）
  up         拉取并启动（--pull missing --no-build，适合使用已构建镜像部署）
  build-up   本地构建并启动
  down       停止并清理容器
  restart    重启（down + up）
  logs       跟踪日志
  ps         容器状态
  config     打印合并后的 compose 配置

示例:
  # 本地构建启动
  ./dockerctl.sh dev build-up

  # 远程镜像仓库（需先 docker login）
  export IMAGE_PREFIX=ghcr.io/<your-org>
  export IMAGE_TAG=latest
  ./dockerctl.sh prod up

  # 自定义密钥文件路径
  export AUTH_ENV_FILE=./service-system.env
  ./dockerctl.sh prod up
EOF
}

is_supported_env() {
  local target="$1"
  local item
  for item in "${SUPPORTED_ENVS[@]}"; do
    if [[ "${item}" == "${target}" ]]; then
      return 0
    fi
  done
  return 1
}

default_image_tag() {
  case "$1" in
  prod) printf 'local-prod\n' ;;
  *) printf 'local\n' ;;
  esac
}

prepare_env() {
  local target_env="$1"
  local default_env_file="${SCRIPT_DIR}/env/service-system.env"

  if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "错误: compose 文件不存在: ${COMPOSE_FILE}"
    exit 1
  fi

  export AUTH_ENV="${target_env}"
  export AUTH_ENV_FILE="${AUTH_ENV_FILE:-${default_env_file}}"
  export IMAGE_TAG="${IMAGE_TAG:-$(default_image_tag "${target_env}")}"
  export SPRING_CLOUD_NACOS_SERVER_ADDR="${SPRING_CLOUD_NACOS_SERVER_ADDR:-nacos:8848}"

  if [[ ! -f "${AUTH_ENV_FILE}" ]]; then
    echo "错误: 缺少环境文件: ${AUTH_ENV_FILE}"
    if [[ "${AUTH_ENV_FILE}" == "${default_env_file}" ]]; then
      echo "请复制 ${SCRIPT_DIR}/env/service-system.env.example 为 service-system.env 后填写。"
    fi
    exit 1
  fi
}

# 仅运行时（远程镜像 / 无本地 build）
dc() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

# 运行时 + 本地 build 覆盖
dc_build() {
  if [[ ! -f "${COMPOSE_OVERRIDE}" ]]; then
    echo "错误: 缺少构建覆盖文件: ${COMPOSE_OVERRIDE}"
    exit 1
  fi
  docker compose -f "${COMPOSE_FILE}" -f "${COMPOSE_OVERRIDE}" "$@"
}

filter_up_args_after_build() {
  up_args=()
  skip_next=0
  for arg in "$@"; do
    if [[ "${skip_next}" -eq 1 ]]; then
      skip_next=0
      continue
    fi
    case "${arg}" in
    --no-cache | --pull | --pull=* | --quiet | -q | --parallel | --with-dependencies | --builder | --builder=* | --progress | --progress=* | --build-arg=* | --ssh | --ssh=* | --memory | --memory=*)
      continue
      ;;
    --build-arg | --progress | --builder | --ssh | --memory)
      skip_next=1
      continue
      ;;
    esac
    up_args+=("${arg}")
  done
}

if [[ "${1:-}" == "help" || "${1:-}" == "-h" || "${1:-}" == "--help" || $# -lt 2 ]]; then
  show_help
  exit 0
fi

TARGET_ENV="$1"
ACTION="$2"
shift 2

if ! is_supported_env "${TARGET_ENV}"; then
  echo "错误: 不支持的环境 '${TARGET_ENV}'（仅支持: ${SUPPORTED_ENVS[*]}）"
  exit 1
fi

prepare_env "${TARGET_ENV}"

case "${ACTION}" in
pull)
  dc pull "$@"
  ;;
build)
  dc_build build "$@"
  ;;
up)
  # 不构建：缺镜像则 pull；已有则用本地（可先 pull 刷新）
  dc up -d --pull missing --no-build "$@"
  ;;
build-up)
  dc_build build "$@"
  filter_up_args_after_build "$@"
  dc_build up -d --build "${up_args[@]}"
  ;;
down)
  dc down "$@"
  ;;
restart)
  dc down "$@"
  dc up -d --pull missing --no-build "$@"
  ;;
logs)
  dc logs -f "$@"
  ;;
ps)
  dc ps "$@"
  ;;
config)
  if [[ -f "${COMPOSE_OVERRIDE}" ]]; then
    dc_build config "$@"
  else
    dc config "$@"
  fi
  ;;
help | -h | --help)
  show_help
  ;;
*)
  echo "错误: 不支持的动作 '${ACTION}'"
  show_help
  exit 1
  ;;
esac
