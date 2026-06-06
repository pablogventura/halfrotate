#!/usr/bin/env bash
# Capture Play Store screenshots from a connected device via adb.
# Usage: ./scripts/capture-store-screenshots.sh [en|es]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/store-assets"
LANG="${1:-en}"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb required." >&2
  exit 1
fi

if ! adb get-state >/dev/null 2>&1; then
  echo "No device connected. Connect the phone and enable USB debugging." >&2
  exit 1
fi

mkdir -p "$OUT"

case "$LANG" in
  en)
    echo "Capture EN screenshot 1: main screen (filter active, scroll to top)."
    read -r -p "Press Enter when ready..."
    adb exec-out screencap -p > "$OUT/screenshot-en-1.png"
    echo "Capture EN screenshot 2: scroll to About section."
    read -r -p "Press Enter when ready..."
    adb exec-out screencap -p > "$OUT/screenshot-en-2.png"
    ;;
  es)
    echo "Capture ES screenshot 1: pantalla principal (filtro activo)."
    read -r -p "Pulsa Enter cuando esté listo..."
    adb exec-out screencap -p > "$OUT/screenshot-es-1.png"
    echo "Capture ES screenshot 2: sección Acerca de visible."
    read -r -p "Pulsa Enter cuando esté listo..."
    adb exec-out screencap -p > "$OUT/screenshot-es-2.png"
    ;;
  *)
    echo "Usage: $0 [en|es]" >&2
    exit 1
    ;;
esac

"$ROOT/scripts/sync-store-assets.sh"
echo "Done. Screenshots in $OUT/"
