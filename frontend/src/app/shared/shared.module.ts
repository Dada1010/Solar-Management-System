import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { MenuModule } from 'primeng/menu';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { EffectiveRateManagerComponent } from './effective-rate-manager/effective-rate-manager.component';

const PRIME_MODULES = [
  AvatarModule,
  ButtonModule,
  DialogModule,
  InputTextModule,
  MenuModule,
  MessageModule,
  PasswordModule,
  PaginatorModule,
  ProgressSpinnerModule,
  TableModule,
  TagModule,
  ToastModule
];

@NgModule({
	declarations: [EffectiveRateManagerComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ...PRIME_MODULES],
  exports: [CommonModule, FormsModule, ReactiveFormsModule, ...PRIME_MODULES, EffectiveRateManagerComponent]
})
export class SharedModule {}