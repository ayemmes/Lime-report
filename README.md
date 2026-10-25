# Lime Report

A small Android app for logging Lime van shifts and counting earnings per shift and per pay week.

## What it does

- **Log a shift** at the end of it: start and end date/time (a shift can start in the evening and end the next morning), and how many vehicles you did of each task.
- **Task types and default rates** (₪ per vehicle, editable under the ⚙ Rates screen):
  - Relocate from a remote spot to a hub: scooter 10, bike 14
  - Deploy from the warehouse to a hub: scooter 8, bike 12
  - Collect a damaged vehicle to the warehouse: 14
- **Shift total** is shown live while you type, and on each shift in the list.
- **Week total**: weeks run Monday 00:00 to Sunday 23:59. A shift counts in the week it *started*, so a Sunday-night shift ending Monday morning belongs to the week that just ended. Use the arrows to see earlier weeks.
- **Voice input**: tap "Say your counts" and speak in English, for example
  "relocated 12 scooters and 5 bikes, deployed 20 scooters and 3 bikes, 4 damaged".
  The fields fill in and you can fix anything before saving.
- **Edit or delete** any shift by tapping it. Each shift keeps the rates it was saved with, so changing rates later only affects new shifts.
- **Share** a week's report as text (WhatsApp, email, etc.) with the share button.

Data is stored only on the phone.

## Getting the APK

### Option A: GitHub builds it for you (no software needed)

1. Push this project to a GitHub repository.
2. GitHub Actions builds the app on every push (see the **Actions** tab). After a push to `main`, the APK is attached to a release called **latest**: open the repository's **Releases** page on your phone and download `LimeReport.apk`.

### Option B: Build on a computer with Android Studio

1. Install [Android Studio](https://developer.android.com/studio).
2. *File → Open* and choose this folder. Wait for the sync to finish.
3. *Build → Build App Bundle(s) / APK(s) → Build APK(s)*. The file appears in `app/build/outputs/apk/`.

Or from a terminal with the Android SDK installed: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.

## Installing on the phone

1. Open the downloaded `LimeReport.apk` on the phone.
2. Android will ask to allow installs from this source (your browser or Files app). Allow it, go back and tap **Install**.
3. Play Protect may warn that the app is from an unknown developer. Choose **Install anyway**.

When you install a newer APK over the old one, your logged shifts are kept.

Requires Android 8.0 or newer.
