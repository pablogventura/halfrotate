#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_HOME
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
AVDMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
AVD_NAME="HalfRotate_API35"
SYSTEM_IMAGE="system-images;android-35;google_apis;x86_64"

echo "ANDROID_HOME=$ANDROID_HOME"

if [[ ! -x "$SDKMANAGER" ]]; then
  echo "sdkmanager not found at $SDKMANAGER" >&2
  exit 1
fi

if ! groups | grep -q '\bkvm\b'; then
  echo "Note: user not in group 'kvm'. If the emulator is slow, run:"
  echo "  sudo usermod -aG kvm \"\$USER\"  # then log out/in"
fi

echo "Accepting SDK licenses..."
yes | "$SDKMANAGER" --licenses >/dev/null || true

echo "Installing emulator packages (may download ~1-2 GB on first run)..."
"$SDKMANAGER" --install \
  "emulator" \
  "$SYSTEM_IMAGE" \
  "platform-tools" \
  "platforms;android-35"

if ! "$AVDMANAGER" list avd | grep -q "Name: $AVD_NAME"; then
  echo "Creating AVD $AVD_NAME..."
  echo no | "$AVDMANAGER" create avd \
    -n "$AVD_NAME" \
    -k "$SYSTEM_IMAGE" \
    -d pixel_6 \
    --force
else
  echo "AVD $AVD_NAME already exists."
fi

echo "Done. Start with: ./scripts/emulator-start.sh"
