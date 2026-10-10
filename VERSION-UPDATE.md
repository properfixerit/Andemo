# IJTC version and update checklist

## Current release configuration

- Gradle project name: `IJTC` (`settings.gradle.kts`)
- App name: `RubelApp` (`app/src/main/res/values/strings.xml`)
- Android application ID: `com.ijtc.rubelapp`
- Android version: `versionName = "1.4"`, `versionCode = 16`
  (`app/build.gradle.kts`)
- Update manifest and APK: GitHub `properfixerit/PFAPK`, `RubelApp/update.json`
  and `RubelApp/app-debug.apk`
- QR/download page: GitHub Pages `RubelApp/index.html`
- Update source: GitHub raw `RubelApp/update.json`. Supabase handles
  registration/login; GitHub hosts the app update and exam data.

## Debug build and device test

Run `.\gradlew.bat assembleDebug` from the project root. This task runs
`testDebugUnitTest` first; after tests pass, it runs
`Install-Update-Run.bat` using the APK at
`app\build\outputs\apk\debug\app-debug.apk` to install/update and launch the app
on a connected Android device. A failed test prevents the
install-and-run step. Updating an already-installed release requires the APK to
use the same signing key; the default debug key will be rejected if it differs.
The installer offers a fresh-install option for a signing mismatch; uninstalling
the existing app erases its local app data.
To build without attempting device installation, add `-PskipInstallAndRun=true`
to the Gradle command.

## To publish the next update

1. Set the next `versionName` and increase `versionCode` in
   `app/build.gradle.kts`. Keep the package name `com.ijtc.rubelapp` if users
   should update the existing app.
2. Keep the login footer in
   `app/src/main/java/com/ijtc/rubelapp/MainActivity.kt` in sync with the
   version display behavior; the version and Supabase project are independent.
3. From `J:\My Drive\AndroidData\RubelApp`, build with
   `.\gradlew.bat assembleRelease`. Sign
   `app\build\outputs\apk\release\app-release-unsigned.apk` with the same
   release key used for the installed app. Verify package, version, and
   signature, then calculate SHA-256 from the final signed APK.
4. For a release build, place the final signed APK at
   `RubelApp/app-debug.apk` in `properfixerit/PFAPK` and update `RubelApp/update.json`.
   The JSON must contain:
   - `versionCode` and `versionName` matching `app/build.gradle.kts`
   - `apkUrl`: `https://raw.githubusercontent.com/properfixerit/PFAPK/main/RubelApp/app-debug.apk`
   - `apkFileName`: `app-debug.apk`
   - `sha256`: lowercase SHA-256 of the final signed APK
5. Update the version shown in `RubelApp/index.html`. Keep the QR code pointing
   at the stable RubelApp download page, then verify GitHub Pages deployment
   and the live version text.
6. Clicking `GitUpload.bat` builds and tests the debug APK, synchronizes the
   version and checksum in `update.json`, and automatically pushes
   `RubelApp/app-debug.apk`, `RubelApp/update.json`, `RubelApp/index.html`, and
   `RubelApp/qr.png`. The page reads its displayed version from the manifest.
   Do not use it for
   release-signed installations; publish the release-signed APK and matching
   manifest checksum instead.

## Package and signing warning

Android treats a changed application ID as a different app. The current ID is
`com.ijtc.rubelapp`. Because the ID changed during the rename, this app installs
separately from previously published installs and does not share their local
data. Preserve the current ID for future RubelApp updates.
Use the existing release keystore for every release. A different signing key
cannot update the installed app, and uninstalling the old app may erase its
data. Always increase `versionCode`; do not reset it for releases intended to
update existing installations.
