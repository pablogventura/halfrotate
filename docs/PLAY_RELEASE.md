# Google Play release checklist

## Before upload

1. Create a [Google Play Developer account](https://play.google.com/console/signup) ($25 one-time).
2. Generate upload keystore:

   ```bash
   keytool -genkey -v -keystore halfrotate-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```

3. Configure release signing in `app/build.gradle.kts` (use `signing.properties`, not committed).
4. Build AAB:

   ```bash
   ./gradlew bundleRelease
   ```

5. Prepare store assets:
   - `store-assets/icon-512.png` (also in fastlane metadata)
   - `store-assets/feature-graphic-1024x500.png`
   - Phone screenshots — replace placeholders with real captures (`adb exec-out screencap -p > shot.png`)
   - Regenerate: `./scripts/generate-store-assets.sh`

## Play Console forms

### Data safety

- **Data collected:** None
- **Data shared:** None
- **Security practices:** Data encrypted in transit N/A; users can request deletion N/A

### Foreground service (Android 14+)

- Type: **Special use** — `screen_rotation_filter`
- Upload a **~30 second video** showing:
  1. User grants Modify system settings
  2. User enables filter
  3. Notification appears
  4. Device rotated to 270° and corrected to 90°

### App content

- Category: Tools
- No ads, no IAP
- Content rating: Everyone / utility

## Listing text highlights

Mention clearly:

- Requires **Modify system settings** for rotation only
- Uses a **foreground service** while filter is active (user-initiated)
- **No data collection**
- Open source: link to GitHub

## Privacy policy URL

Host `privacy-policy/index.html` via GitHub Pages, e.g.:

`https://pablogventura.github.io/halfrotate/`

Enable Pages: repo Settings → Pages → Build and deployment → GitHub Actions (workflow `.github/workflows/pages.yml`).

## Rollout

1. Internal testing track
2. Closed testing (optional)
3. Production

## GPL note

You must offer source code to users. Link to GitHub in the store listing and in-app About screen (already implemented).
