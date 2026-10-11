# RubelApp

Complete Android app and backend source for `com.ijtc.rubelapp`.
The project builds and tests in GitHub Actions, installs the debug APK in an Android emulator,
publishes the emulator-tested APK for download, and can upload it to Appetize for interactive
browser testing.

## Test and download

1. Open [Run or view the Android emulator test](https://github.com/properfixerit/Andemo/actions/workflows/rubelapp-android.yml).
2. Select **Run workflow** to start a test, or open the latest completed run.
3. Open the **Run RubelApp in your browser** link in the run summary for the interactive Appetize
   emulator. You can also download the **rubelapp-debug** artifact from the run's **Artifacts**
   section or use the
   [latest emulator-tested RubelApp APK](https://github.com/properfixerit/Andemo/releases/download/test-latest/RubelApp-debug.apk).

Each run on `main` builds the app and uploads a debug APK before installing and launching it on a
Pixel 2 Android 15 emulator. When the Appetize API token is configured, the run also uploads the
APK to Appetize and adds a browser-playable link to the run summary. It always refreshes the
download link above after a successful emulator test.
The APK is a debug build, not a Play Store release. Android displays a warning when installing
debug APKs from outside the Play Store.

## Appetize browser emulator

To publish a browser-playable APK from GitHub Actions, add an Appetize API token as the
`APPETIZE_API_TOKEN` repository Actions secret. Create one in the
[Appetize organization API Tokens settings](https://appetize.io/organization/api-tokens).
The first upload creates an Appetize app and includes its browser link in the
workflow run summary. To update the same Appetize app on later runs, save that app's public key
as the `APPETIZE_PUBLIC_KEY` repository Actions secret.

If `APPETIZE_API_TOKEN` is not configured, the build, APK download, and GitHub emulator test
still run; only the Appetize upload is skipped.

The emulator smoke test verifies that the app installs and its main activity starts. The
RubelApp backend is not installed or configured in the emulator.

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
