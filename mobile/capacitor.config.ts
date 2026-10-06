Changeimport type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.aditya.solarmanagement',
  appName: 'Aditya Solar Management',
  webDir: '../frontend/dist/solar-management-frontend/browser',
  server: {
    androidScheme: 'https',
    cleartext: true
  },
  android: {
    allowMixedContent: true
  }
};

export default config;
