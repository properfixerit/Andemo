# RubelApp

Complete Android app and backend source for `com.ijtc.rubelapp`.
The project builds and tests in GitHub Actions, installs the debug APK in an Android emulator,
and publishes the emulator-tested APK for download.

## Test and download

1. Open [Run or view the Android emulator test](https://github.com/properfixerit/Andemo/actions/workflows/rubelapp-android.yml).
2. Select **Run workflow** to start a test, or open the latest completed run.
3. Download the **rubelapp-debug** artifact from the run's **Artifacts** section. Successful
   emulator runs also publish the
   [latest emulator-tested RubelApp APK](https://github.com/properfixerit/Andemo/releases/download/test-latest/RubelApp-debug.apk).

Each run on `main` builds the app and uploads a debug APK before installing and launching it on a
Pixel 2 Android 15 emulator. A successful emulator run also refreshes the APK at the download link above.
The APK is a debug build, not a Play Store release. Android displays a warning when installing
debug APKs from outside the Play Store.

The emulator smoke test verifies that the app installs and its main activity starts. The
`com.ijtc.rubelapp` app and its backend are not installed or configured in the emulator.

## Local Android build

Install Java 25 and Android SDK 37, then run:

```text
gradlew.bat -PskipInstallAndRun=true assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
`-PskipInstallAndRun=true` skips the optional local-device install task.

## Backend configuration

Keep `local.properties` out of source control. To enable the RubelApp Supabase Edge Function
in local builds, put the project's URL and publishable key in that ignored file:

```properties
supabase.rubelapp.url=https://YOUR_PROJECT_REF.supabase.co
supabase.rubelapp.publishableKey=YOUR_PROJECT_PUBLISHABLE_KEY
```

GitHub Actions does not require backend credentials to build or emulator-test the app. To
enable the backend in GitHub-built APKs, configure the repository Actions variables
`RUBELAPP_SUPABASE_URL` and `RUBELAPP_SUPABASE_PUBLISHABLE_KEY`. Never add service-role
keys, GitHub tokens, SMTP passwords, or other private credentials to the app or repository.

The app's `ijtc` function allows unauthenticated writes to its documented, restricted file
allowlist. Review `supabase/functions/ijtc/index.ts` before deploying or using that backend.

## Deploy the Supabase function

The optional **Deploy RubelApp Edge Function** workflow deploys the separate `rubelapp`
Supabase function when manually run. Configure `SUPABASE_ACCESS_TOKEN` and
`SUPABASE_PROJECT_REF` as GitHub Actions secrets first.
