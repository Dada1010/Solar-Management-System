# Frontend Setup

Angular 21 web application shared by the browser app and the Capacitor mobile app.

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.25.

## Local Development

Requirements: Node.js compatible with Angular 21 and npm 10.9 or newer. From the `frontend` directory, install dependencies and start the development server:

```powershell
npm ci
npm start -- --host 127.0.0.1
```

Open `http://localhost:4200`. `npm start` uses `src/environments/environment.local.ts` and the local API at `http://localhost:8081/api/v1`. To use the remote dev backend, start with `npm start -- --configuration development`; it targets `http://103.120.179.127:8081/api/v1`.

## Production and Mobile Configuration

The production build replaces `src/environments/environment.ts` with `src/environments/environment.production.ts` and targets `http://103.120.179.127:8082/api/v1`. The mobile build uses `src/environments/environment.mobile.ts` and targets the dev API at `http://103.120.179.127:8081/api/v1`.

The mobile build uses `src/environments/environment.mobile.ts`; the APK build steps are in `../mobile/README.md`.

```powershell
npm run build
npm test
```

Build output is written to `dist/solar-management-frontend/browser`. Environment values are bundled into browser JavaScript, so never put credentials or other secrets in these files.

Environment files:

- `src/environments/environment.ts`: remote dev API URL.
- `src/environments/environment.local.ts`: localhost API URL used by `npm start`.
- `src/environments/environment.production.ts`: production web API URL.
- `src/environments/environment.mobile.ts`: Android/Capacitor API URL.

