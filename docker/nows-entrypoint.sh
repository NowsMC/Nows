#!/usr/bin/env bash
set -euo pipefail

original_dir="$PWD"

cd /workspace

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/workspace/.gradle-docker}"
export npm_config_cache="${npm_config_cache:-/workspace/.npm}"

mkdir -p "$GRADLE_USER_HOME" "$npm_config_cache" /workspace/.nows

if [[ -f ./gradlew ]]; then
  chmod +x ./gradlew
fi

required_submodules=(
  repos/NowsApiMod
  repos/NowsGradlePlugin
  repos/NowsInstaller
  repos/NowsRemapper
)

missing_submodules=()
for submodule in "${required_submodules[@]}"; do
  if [[ ! -f "$submodule/build.gradle.kts" ]]; then
    missing_submodules+=("$submodule")
  fi
done

if (( ${#missing_submodules[@]} > 0 )) && [[ -f .gitmodules ]] && command -v git >/dev/null 2>&1; then
  git -c url.https://github.com/.insteadOf=git@github.com: \
    submodule update --init --recursive "${missing_submodules[@]}"
fi

if [[ "$original_dir" == /workspace/repos/NowsWeb* ]] && [[ ! -f /workspace/repos/NowsWeb/package.json ]]; then
  echo "NowsWeb is optional/private and is not checked out in this workspace." >&2
  echo "Skip the web profile, or check out repos/NowsWeb separately if you have access." >&2
  exit 1
fi

cd "$original_dir"

exec "$@"
