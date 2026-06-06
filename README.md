# HalfRotate

Limit Android auto-rotation to **chosen orientations** without root — portrait, landscape, or custom presets with an anti-flicker engine.

Free, no ads, no tracking. Licensed under **GPL-3.0-or-later**.

## Features

- **Four rotation presets:** portrait + landscape (default), portrait only, landscape only, all except upside down
- **Force auto-rotation:** keep rotating between allowed orientations even when system auto-rotate is off
- **Anti-flicker engine:** locks system auto-rotate while filtering and transitions directly between allowed orientations (no flash through blocked angles)
- Foreground service only while the filter is active
- Quick Settings tile
- Restores after reboot (with battery/autostart configured on MIUI)
- English and Spanish

## Build

Requirements: JDK 17+, Android SDK 35.

```bash
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (unsigned)
./gradlew bundleRelease          # release AAB (unsigned)
./gradlew test                   # unit tests
```

Set `sdk.dir` in `local.properties` or `ANDROID_HOME`.

### Release signing

Create a keystore and add to `signing.properties` (not committed):

```properties
storeFile=/path/to/halfrotate-upload.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Then configure `signingConfigs` in `app/build.gradle.kts` before publishing.

Host `privacy-policy/index.html` via GitHub Pages (workflow included):

`https://pablogventura.github.io/halfrotate/`

Enable: repo Settings → Pages → Source: GitHub Actions.

## Store assets

```bash
./scripts/generate-store-assets.sh   # icon 512, feature graphic, placeholder screenshots
```

Output: `store-assets/` and `fastlane/metadata/android/*/images/`. Replace screenshots with real `adb exec-out screencap -p` captures before publishing.

## Install (development)

```bash
./scripts/install-debug.sh
```

Grant **Modify system settings** in the app, choose a rotation preset, then enable the filter.

## Permissions

| Permission | Why |
|------------|-----|
| `WRITE_SETTINGS` | Read and set `user_rotation` / `accelerometer_rotation` |
| `FOREGROUND_SERVICE` / `SPECIAL_USE` | Keep filter active in background |
| `POST_NOTIFICATIONS` | Show active filter notification (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Restore filter after reboot |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional; improves reliability on MIUI |

HalfRotate **does not** collect, transmit, or sell any personal data.

## Distribution

| Channel | Format | Signing |
|---------|--------|---------|
| [Google Play](docs/PLAY_RELEASE.md) | AAB | Play App Signing |
| [F-Droid](metadata/fdroid/dev.pablo.halfrotate.yml) | APK | F-Droid key (or reproducible with yours) |

**Note:** Play Store and F-Droid builds use different signing keys. Switching channels requires reinstalling the app.

## Source code

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

See [COPYING](COPYING) for the full license text.

## Privacy

[Privacy policy](privacy-policy/index.html) — also publish to GitHub Pages for store listings.

## F-Droid submission

1. Tag a release: `git tag v1.1.0 && git push origin v1.1.0`
2. Open a merge request to [fdroiddata](https://gitlab.com/fdroid/fdroiddata) with `metadata/dev.pablo.halfrotate.yml`
3. Or file a [Request for Packaging](https://gitlab.com/fdroid/rfp/-/issues/new)

See [docs/FDROID.md](docs/FDROID.md) for details.

## Limitations

- Some apps that force their own orientation may override system rotation
- Manual QA on target devices (e.g. Xiaomi/MIUI) is recommended after each release

## Testing

```bash
./gradlew test                           # unit tests (JVM, no device)
./scripts/setup-android-emulator.sh      # once: install API 35 emulator (~1-2 GB)
./scripts/emulator-start.sh              # GUI emulator (headless: ./scripts/emulator-start.sh headless)
./scripts/emulator-smoke-test.sh         # emulator + instrumented tests (debug APK)
./scripts/run-all-tests.sh               # unit + emulator smoke
```

Grant `WRITE_SETTINGS` is handled automatically in instrumented tests via `appops`. Debug builds include a test-only broadcast receiver for reliable filter enable/disable.
