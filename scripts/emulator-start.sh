#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_HOME
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

AVD_NAME="HalfRotate_API35"
MODE="${1:-gui}"
PID_FILE="/tmp/halfrotate-emulator.pid"

if [[ "$MODE" == "headless" ]]; then
  EMULATOR_ARGS=(-no-window)
else
  EMULATOR_ARGS=()
fi

if adb devices | grep -q '^emulator-'; then
  echo "Emulator already connected."
else
  if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "Emulator process already running (pid $(cat "$PID_FILE"))."
  else
    echo "Starting emulator $AVD_NAME (${MODE})..."
    nohup emulator -avd "$AVD_NAME" -no-snapshot-save "${EMULATOR_ARGS[@]}" >/tmp/halfrotate-emulator.log 2>&1 &
    echo $! >"$PID_FILE"
  fi
fi

echo "Waiting for device..."
adb wait-for-device

boot_completed=""
for _ in $(seq 1 120); do
  boot_completed="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [[ "$boot_completed" == "1" ]]; then
    echo "Emulator booted."
    exit 0
  fi
  sleep 2
done

echo "Timed out waiting for emulator boot. See /tmp/halfrotate-emulator.log" >&2
exit 1
