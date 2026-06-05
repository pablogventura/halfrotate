# Request for Packaging — HalfRotate

Use this template when opening an issue at https://gitlab.com/fdroid/rfp/-/issues/new

---

- **App name:** HalfRotate
- **Package ID:** dev.pablo.halfrotate
- **Source code:** https://github.com/pablogventura/halfrotate
- **License:** GPL-3.0-or-later
- **Category:** System utility — limits auto-rotation to portrait and landscape (blocks 180° and 270°)

## Description

HalfRotate keeps Android auto-rotate enabled but corrects disallowed orientations via `WRITE_SETTINGS`. No root, no ads, no tracking.

## Metadata

A draft metadata file is included in the repository:

`metadata/fdroid/dev.pablo.halfrotate.yml`

## Build

```bash
./gradlew assembleRelease
```

Release tag: `v1.0.0`

## Permissions justification

| Permission | Purpose |
|------------|---------|
| WRITE_SETTINGS | Read/correct user_rotation only |
| FOREGROUND_SERVICE (specialUse) | Active while user-enabled filter runs |
| RECEIVE_BOOT_COMPLETED | Restore filter after reboot if enabled |
| POST_NOTIFICATIONS | Show filter active notification (Android 13+) |

## Contact

GitHub Issues: https://github.com/pablogventura/halfrotate/issues
