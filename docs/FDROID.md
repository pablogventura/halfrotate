# F-Droid packaging notes

## Submit

1. Ensure tag `v1.1.0` exists on GitHub with matching `versionCode` / `versionName` in `app/build.gradle.kts`.
2. Copy or symlink [`metadata/fdroid/dev.pablo.halfrotate.yml`](../metadata/fdroid/dev.pablo.halfrotate.yml) into the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) `metadata/` directory.
3. Open a merge request, or file an [RFP issue](https://gitlab.com/fdroid/rfp/-/issues/new).

## Store metadata in repo

F-Droid reads Fastlane/Triple-T files from:

- `fastlane/metadata/android/en-US/`
- `fastlane/metadata/android/es-ES/`

Add screenshots under `fastlane/metadata/android/en-US/images/phoneScreenshots/` before release.

## Reproducible builds (optional)

- Pinned dependency versions in `gradle/libs.versions.toml`
- `dependenciesInfo { includeInApk = false }` in `app/build.gradle.kts`
- Document exact JDK (17) and AGP version in README

After your first signed release APK, you may add `AllowedAPKSigningKeys` and `Binaries` to the fdroid metadata.

## Signing

Default F-Droid builds sign with an F-Droid key. Play Store uses a different key — users cannot switch without reinstalling.
