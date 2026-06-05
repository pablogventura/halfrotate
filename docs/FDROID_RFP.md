# Request for Packaging — HalfRotate

Use this template when opening an issue at https://gitlab.com/fdroid/rfp/-/issues/new

---

- **App name:** HalfRotate
- **Package ID:** dev.pablo.halfrotate
- **Source code:** https://github.com/pablogventura/halfrotate
- **License:** GPL-3.0-or-later
- **Category:** System utility — filters auto-rotation to user-chosen orientations (presets)

## Description

HalfRotate filters Android system auto-rotation via `WRITE_SETTINGS`: four presets, optional force auto-rotation, and a proactive anti-flicker engine. No root, no ads, no tracking.

## Metadata

A draft metadata file is included in the repository:

`metadata/fdroid/dev.pablo.halfrotate.yml`

## Build

```bash
./gradlew assembleRelease
```

Release tag: `v1.1.0`

## Permissions justification

| Permission | Purpose |
|------------|---------|
| WRITE_SETTINGS | Read/set user_rotation and accelerometer_rotation only |
| FOREGROUND_SERVICE (specialUse) | Active while user-enabled filter runs |
| RECEIVE_BOOT_COMPLETED | Restore filter after reboot if enabled |
| POST_NOTIFICATIONS | Show filter active notification (Android 13+) |

## Contact

GitHub Issues: https://github.com/pablogventura/halfrotate/issues
