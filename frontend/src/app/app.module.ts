import { NgModule, provideZoneChangeDetection } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { definePreset } from '@primeng/themes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { MessageService } from 'primeng/api';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthModule } from './auth/auth.module';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { WorkspaceModule } from './workspace/workspace.module';

const AdityaTheme = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#eff7f1', 100: '#d8eadc', 200: '#b8d8c2', 300: '#8ebf9f',
      400: '#63a17a', 500: '#42835d', 600: '#326b4a', 700: '#28553d',
      800: '#214432', 900: '#1b392b', 950: '#10251b'
    }
  }
});

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    AuthModule,
    CoreModule,
    SharedModule,
    WorkspaceModule
  ],
  providers: [
    provideZoneChangeDetection(),
    MessageService,
    providePrimeNG({
      theme: {
        preset: AdityaTheme,
        options: { darkModeSelector: 'none' }
      }
    })
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}