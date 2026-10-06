import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-first-login',
  templateUrl: './first-login.component.html',
  styleUrl: '../auth.component.scss',
  standalone: false
})
export class FirstLoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  readonly form = this.formBuilder.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  });
  loading = false;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly messages: MessageService
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword, confirmPassword } = this.form.getRawValue();
    if (newPassword !== confirmPassword) {
      this.messages.add({ severity: 'warn', summary: 'Passwords do not match', detail: 'Enter the same new password twice.' });
      return;
    }
    this.loading = true;
    this.auth.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: (error: unknown) => {
        this.loading = false;
        const detail = error instanceof Error ? error.message : 'Password update failed. Try again.';
        this.messages.add({ severity: 'error', summary: 'Could not update password', detail });
      }
    });
  }
}