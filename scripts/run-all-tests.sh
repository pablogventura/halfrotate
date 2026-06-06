#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "== Unit tests (JVM) =="
./gradlew test --no-daemon

echo "== Emulator instrumented tests =="
"$ROOT/scripts/emulator-smoke-test.sh"

echo "All tests passed."
