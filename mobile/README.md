# Aditya Solar Management Mobile

The Android app is a Capacitor shell around the existing Angular application. Screens and API workflows stay shared with `frontend/`.

## Android emulator

From this directory:

```powershell
npm install
npm run sync
npm run open:android
```

The mobile Angular build uses `http://10.0.2.2:8080/api/v1`, which reaches the host machine's backend from the standard Android emulator. This cleartext URL is for local development only. For a physical device, replace that host with the development machine's LAN IP and allow the device through the firewall. Use an HTTPS API and HTTPS Android scheme for release builds.

Android Studio and the Android SDK are required to build or run the native project. Configure `ANDROID_HOME` before running `npm run run:android`.
