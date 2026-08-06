#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$ROOT/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

VERSION=9.1.0
EXPECTED_SHA256=a17ddd85a26b6a7f5ddb71ff8b05fc5104c0202c6e64782429790c933686c806
BOOT="$ROOT/.gradle-bootstrap"
DIST="$BOOT/gradle-$VERSION"
ZIP="$BOOT/gradle-$VERSION-bin.zip"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$BOOT"
  URL="https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
  echo "Nows bootstrap: downloading Gradle $VERSION..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$URL"
  else
    echo "curl or wget is required for the first Gradle bootstrap." >&2
    exit 1
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL_SHA256=$(sha256sum "$ZIP" | awk '{print $1}')
  elif command -v shasum >/dev/null 2>&1; then
    ACTUAL_SHA256=$(shasum -a 256 "$ZIP" | awk '{print $1}')
  else
    echo "sha256sum or shasum is required to verify the Gradle distribution." >&2
    exit 1
  fi
  if [ "$ACTUAL_SHA256" != "$EXPECTED_SHA256" ]; then
    echo "Gradle distribution SHA-256 mismatch." >&2
    rm -f "$ZIP"
    exit 1
  fi
  rm -rf "$DIST"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$ZIP" -d "$BOOT"
  else
    echo "unzip is required for the first Gradle bootstrap." >&2
    exit 1
  fi
fi
exec "$DIST/bin/gradle" "$@"
