#!/usr/bin/env bash
# Copy store-assets into fastlane metadata (icons, feature graphics, screenshots).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/store-assets"
FASTLANE="$ROOT/fastlane/metadata/android"

mkdir -p \
  "$FASTLANE/en-US/images/phoneScreenshots" \
  "$FASTLANE/es-ES/images/phoneScreenshots"

cp "$OUT/icon-512.png" "$FASTLANE/en-US/images/icon.png"
cp "$OUT/icon-512.png" "$FASTLANE/es-ES/images/icon.png"
cp "$OUT/feature-graphic-1024x500.png" "$FASTLANE/en-US/images/featureGraphic.png"
cp "$OUT/feature-graphic-1024x500.png" "$FASTLANE/es-ES/images/featureGraphic.png"

for i in 1 2; do
  if [[ -f "$OUT/screenshot-en-${i}.png" ]]; then
    cp "$OUT/screenshot-en-${i}.png" "$FASTLANE/en-US/images/phoneScreenshots/${i}.png"
  fi
  if [[ -f "$OUT/screenshot-es-${i}.png" ]]; then
    cp "$OUT/screenshot-es-${i}.png" "$FASTLANE/es-ES/images/phoneScreenshots/${i}.png"
  fi
done

echo "Synced store assets to fastlane/metadata/android/"
