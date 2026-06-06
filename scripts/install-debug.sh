#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

if [[ ! -f "$APK" ]]; then
  echo "Building debug APK..."
  (cd "$ROOT" && ./gradlew assembleDebug --no-daemon)
fi

echo "Installing $APK"
echo "On MIUI: enable USB install when prompted on the phone."
adb install -r "$APK"
