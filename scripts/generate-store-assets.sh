#!/usr/bin/env bash
# Generate Play Store / F-Droid graphic assets with ImageMagick.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/store-assets"
mkdir -p "$OUT"

BG="#1B5E20"
FG="#FFFFFF"
ACCENT="#81C784"

if ! command -v convert >/dev/null 2>&1; then
  echo "ImageMagick (convert) required." >&2
  exit 1
fi

# 512x512 Play Store icon
convert -size 512x512 "xc:$BG" \
  -fill "$ACCENT" -draw "rectangle 180,180 332,332" \
  -fill "$FG" -draw "polygon 256,140 340,224 310,224 310,292 202,292 202,224 172,224" \
  -fill "$ACCENT" -draw "polygon 140,256 224,340 224,310 292,310 292,202 224,202 224,172" \
  "$OUT/icon-512.png"

# 1024x500 feature graphic
convert -size 1024x500 "xc:$BG" \
  -gravity center -fill "$FG" -pointsize 72 -annotate +0-40 "HalfRotate" \
  -pointsize 28 -annotate +0+50 "Portrait + landscape auto-rotate only" \
  -fill "$ACCENT" -draw "roundrectangle 700,150 950,350 20,20" \
  "$OUT/feature-graphic-1024x500.png"

# Placeholder phone screenshots (replace with ./scripts/capture-store-screenshots.sh)
for lang in en es; do
  for num in 1 2; do
    label="HalfRotate"
    if [[ "$num" == "1" ]]; then
      subtitle="Filter active — portrait & landscape"
      [[ "$lang" == "es" ]] && subtitle="Filtro activo — vertical y horizontal"
    else
      subtitle="About — source code & privacy"
      [[ "$lang" == "es" ]] && subtitle="Acerca de — código y privacidad"
    fi
    convert -size 1080x2400 "xc:#121212" \
      -fill "$BG" -draw "rectangle 0,0 1080,280" \
      -gravity north -fill "$FG" -pointsize 48 -annotate +0+100 "$label" \
      -gravity center -fill "$ACCENT" -pointsize 36 -annotate +0+0 "$subtitle" \
      -fill "#888888" -pointsize 24 -annotate +0+200 "(Replace with adb screencap)" \
      "$OUT/screenshot-${lang}-${num}.png"
  done
done

echo "Wrote assets to $OUT/"
ls -la "$OUT"

"$ROOT/scripts/sync-store-assets.sh"
