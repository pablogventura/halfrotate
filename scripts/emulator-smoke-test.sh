#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

resolve_emulator_serial() {
  adb devices | awk '/^emulator-/{print $1; exit}'
}

ANDROID_SERIAL="$(resolve_emulator_serial)"
if [[ -z "$ANDROID_SERIAL" ]]; then
  echo "No emulator found. Run ./scripts/emulator-start.sh first." >&2
  exit 1
fi
export ANDROID_SERIAL
echo "Using device $ANDROID_SERIAL"

"$ROOT/scripts/setup-android-emulator.sh"
"$ROOT/scripts/emulator-start.sh"

echo "Running instrumented tests on $ANDROID_SERIAL..."
./gradlew assembleDebug connectedDebugAndroidTest --no-daemon

echo "Smoke tests passed."
