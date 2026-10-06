# Mobile Setup and APK Build

The Android app is a Capacitor shell around the Angular application in `../frontend`; screens and API workflows are shared.

## Requirements

- Node.js and npm
- Java 21
- Android Studio and Android SDK, including an Android platform and build tools
- Android emulator or USB-connected Android device to install/run the app

## Local Emulator Run

Start the backend on port `8080`, then from the `mobile` directory run:

```powershell
npm install
npm run sync
npm run open:android
```

The mobile build selects `../frontend/src/environments/environment.mobile.ts`. Its default API URL, `http://10.0.2.2:8080/api/v1`, reaches port 8080 on the host computer from the standard Android emulator. Android Studio can then build and launch the app on a selected emulator.

After setting the Android SDK environment variables below, `npm run run:android` syncs and launches through the Capacitor CLI.

## Build a Debug APK

From the `mobile` directory, sync the latest web build and set the SDK path for the current PowerShell session. Adjust the SDK path if it is installed elsewhere.

```powershell
npm run sync
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\android\gradlew.bat -p .\android assembleDebug
```

The APK is created at `android/app/build/outputs/apk/debug/app-debug.apk`. This is a debug build for development/testing, not a release-signed store build. Do not commit `android/local.properties`; it contains a machine-specific SDK path.

## Development and Production API Configuration

- Emulator development: update `../frontend/src/environments/environment.mobile.ts`; `10.0.2.2` is the standard Android emulator's host-machine alias.
- Physical-device development: set that file's `apiBaseUrl` to the development computer's reachable LAN address and port, allow the connection through the firewall, and allow the app origin in backend CORS settings.
- Production APK: set `apiBaseUrl` to the deployed HTTPS API before syncing/building. Also update `capacitor.config.ts` to use the HTTPS Android scheme and disable cleartext traffic. Review `android/app/src/main/AndroidManifest.xml` and remove development-only cleartext access. Configure secure APK signing for release distribution; do not store signing credentials or keystores in source control.

The Angular mobile build is selected by the `mobile` configuration in `../frontend/angular.json`. `npm run sync` builds that configuration and copies its output into the Android project. Capacitor app ID/name and Android scheme are configured in `capacitor.config.ts`.
