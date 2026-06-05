# Changelog

All notable changes to HalfRotate are documented in this file.

## [1.1.0] - 2026-06-05

### Added

- Four rotation presets: portrait + landscape (default), portrait only, landscape only, all except upside down.
- "Force auto-rotation" toggle to keep rotating between allowed orientations when system auto-rotate is off.
- Configuration section on the main screen with live preset changes.
- Proactive anti-flicker engine: locks system auto-rotate while filtering and transitions directly between allowed orientations with sensor hysteresis.

### Changed

- Replaced reactive rotation correction with sensor-based routing (no flash through blocked angles).
- Restores previous system auto-rotate setting when the filter is disabled.
- Updated FAQ for anti-flicker behavior and app-specific orientation locks.

## [1.0.0] - 2026-06-05

### Added

- Filter system auto-rotation to portrait (0°) and landscape (90°) only.
- Block correction for upside-down (180°) and reverse landscape (270°).
- Foreground service with persistent notification while filter is active.
- Quick Settings tile to toggle the filter.
- Boot receiver to restore filter after reboot.
- English and Spanish UI.
- MIUI / Xiaomi battery and autostart guidance.
- GPL-3.0-or-later license.

[1.1.0]: https://github.com/pablogventura/halfrotate/releases/tag/v1.1.0
[1.0.0]: https://github.com/pablogventura/halfrotate/releases/tag/v1.0.0
