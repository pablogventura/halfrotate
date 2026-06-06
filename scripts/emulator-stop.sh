#!/usr/bin/env bash
set -euo pipefail

PID_FILE="/tmp/halfrotate-emulator.pid"

if adb devices | grep -q '^emulator-'; then
  adb emu kill || true
fi

if [[ -f "$PID_FILE" ]]; then
  pid="$(cat "$PID_FILE")"
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid" || true
  fi
  rm -f "$PID_FILE"
fi

echo "Emulator stopped."
