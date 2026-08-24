#!/usr/bin/env bash
# 本仓升版本号 + annotated tag。不创建 GitHub Release、不打包 kit。
#
# 用法:
#   ./bump-version.sh 1.0.0-beta.5
#   ./bump-version.sh 1.0.0-beta.5 --push
#   ./bump-version.sh 1.0.0-beta.5 --dry-run
#
# 产品发版请用上一级编排脚本（闭源 auth-pro/、开源 auth-community/），以保持前后端同号。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NEW=""
DO_PUSH=0
DRY_RUN=0
KIND=""
VERSION_FILE=""

usage() {
  cat >&2 <<'EOF'
用法: ./bump-version.sh <semver> [--push] [--dry-run]

本脚本只改本仓版本文件并打 tag，不发布 GitHub Release。
产品号须前后端一致，日常请用上一级 bump-community-version.sh。
EOF
  exit 1
}

reject_release_flags() {
  echo "错误: 本脚本不发布 GitHub Release（不支持 $1）。" >&2
  echo "打包上传请用 Auth/assets/upload-kit-release.sh community <tag> --create --upload" >&2
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --push) DO_PUSH=1 ;;
    --dry-run) DRY_RUN=1 ;;
    --kit|--kit-web|--release|--prerelease|--notes-file|--sync-webdoc)
      reject_release_flags "$1"
      ;;
    -h|--help) usage ;;
    -*)
      echo "未知参数: $1" >&2
      usage
      ;;
    *)
      if [[ -n "${NEW}" ]]; then
        echo "错误: 只能指定一个版本号，多余参数: $1" >&2
        usage
      fi
      NEW="$1"
      ;;
  esac
  shift
done

[[ -n "${NEW}" ]] || usage
NEW="${NEW#v}"
[[ "${NEW}" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-].+)?$ ]] || {
  echo "非法版本: ${NEW}（示例: 1.0.0-beta.5）" >&2
  exit 1
}
TAG="v${NEW}"

detect_kind() {
  if [[ -f "${ROOT}/pom.xml" ]] && grep -q '<revision>' "${ROOT}/pom.xml"; then
    KIND="maven"
    VERSION_FILE="pom.xml"
  elif [[ -f "${ROOT}/package.json" ]]; then
    KIND="pnpm"
    VERSION_FILE="package.json"
  else
    echo "错误: ${ROOT} 无法识别版本文件（需要 pom.xml <revision> 或 package.json）" >&2
    exit 1
  fi
}

current_version() {
  if [[ "${KIND}" == "maven" ]]; then
    sed -nE 's/^[[:space:]]*<revision>([^<]+)<\/revision>.*/\1/p' "${ROOT}/pom.xml" | head -1
  else
    (cd "${ROOT}" && node -p "require('./package.json').version")
  fi
}

assert_git_repo() {
  [[ -d "${ROOT}/.git" ]] || { echo "错误: ${ROOT} 不是 git 仓库" >&2; exit 1; }
}

assert_clean() {
  if [[ -n "$(git -C "${ROOT}" status --porcelain)" ]]; then
    echo "错误: ${ROOT} 工作区不干净，请先提交或贮藏" >&2
    git -C "${ROOT}" status --short >&2
    exit 1
  fi
}

tag_exists() {
  git -C "${ROOT}" rev-parse -q --verify "refs/tags/${TAG}" >/dev/null 2>&1
}

finish_if_already_tagged() {
  tag_exists || return 0
  local cur
  cur="$(current_version)"
  if [[ "${cur}" != "${NEW}" ]]; then
    echo "错误: 已存在 tag ${TAG}，但当前版本是 ${cur}" >&2
    exit 1
  fi
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    echo "已是 ${TAG}，无需再 bump。"
    exit 0
  fi
  if [[ "${DO_PUSH}" -eq 1 ]]; then
    echo "已是 ${TAG}，仅 push"
    push_ref
    exit 0
  fi
  echo "已是 ${TAG}。加 --push 即可推送。"
  exit 0
}

bump_files() {
  local cur
  cur="$(current_version)"
  if [[ "${cur}" == "${NEW}" ]]; then
    echo "版本已是 ${NEW}，跳过改文件"
    return 0
  fi
  if [[ "${KIND}" == "maven" ]]; then
    sed -i -E "s#(<revision>)[^<]+(</revision>)#\1${NEW}\2#" "${ROOT}/pom.xml"
  else
    (cd "${ROOT}" && pnpm version "${NEW}" --no-git-tag-version)
  fi
}

commit_and_tag() {
  cd "${ROOT}"
  git add "${VERSION_FILE}"
  if git diff --cached --quiet; then
    local cur
    cur="$(current_version)"
    if [[ "${cur}" != "${NEW}" ]]; then
      echo "错误: 版本文件无变更，且当前版本 ${cur} ≠ ${NEW}" >&2
      exit 1
    fi
    echo "版本已是 ${NEW}，仅打 tag"
  else
    git commit -m "$(cat <<EOF
:bookmark: release: bump version to ${NEW}

1. 将版本号更新为 ${NEW}
EOF
)"
  fi
  git tag -a "${TAG}" -m "${TAG}"
  echo "[$(basename "${ROOT}")] 已 tag ${TAG}"
}

push_ref() {
  git -C "${ROOT}" push origin HEAD
  git -C "${ROOT}" push origin "${TAG}"
  echo "[$(basename "${ROOT}")] 已 push HEAD 与 ${TAG}"
}

print_plan() {
  local actions="commit + annotated tag"
  if [[ "${DO_PUSH}" -eq 1 ]]; then
    actions+=" + push"
  fi
  echo "仓: $(basename "${ROOT}")  类型: ${KIND}"
  echo "当前: $(current_version)  →  ${NEW}  tag: ${TAG}"
  echo "会改: ${ROOT}/${VERSION_FILE}"
  echo "动作: ${actions}"
}

detect_kind
assert_git_repo
finish_if_already_tagged

echo "==> bump $(basename "${ROOT}") to ${NEW} (tag ${TAG})"
print_plan

if [[ "${DRY_RUN}" -eq 1 ]]; then
  if [[ -n "$(git -C "${ROOT}" status --porcelain)" ]]; then
    echo "警告: 工作区不干净，正式执行会失败。" >&2
  fi
  echo "dry-run：未执行。"
  exit 0
fi

assert_clean

bump_files
commit_and_tag

if [[ "${DO_PUSH}" -eq 1 ]]; then
  push_ref
elif [[ -z "${AUTH_BUMP_ORCHESTRATED:-}" ]]; then
  echo "完成。未加 --push，请自行: git -C ${ROOT} push && git -C ${ROOT} push origin ${TAG}"
fi
