import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { LoginComponent } from './login/login.component';
import { FirstLoginComponent } from './first-login/first-login.component';

@NgModule({
  declarations: [LoginComponent, FirstLoginComponent],
  imports: [SharedModule, RouterModule],
  exports: [LoginComponent, FirstLoginComponent]
})
export class AuthModule {}