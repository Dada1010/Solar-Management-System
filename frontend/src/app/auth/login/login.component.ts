import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: '../auth.component.scss',
  standalone: false
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  readonly form = this.formBuilder.nonNullable.group({
    emailAddress: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
  loading = false;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly messages: MessageService
  ) {
    if (auth.isAuthenticated) {
      void router.navigate([auth.mustChangePassword ? '/first-login' : '/companies']);
    }
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const { emailAddress, password } = this.form.getRawValue();
    this.auth.login(emailAddress, password).subscribe({
      next: (session) => {
        this.loading = false;
        void this.router.navigate([session.mustChangePassword ? '/first-login' : '/companies']);
      },
      error: (error: unknown) => {
        this.loading = false;
        this.messages.add({
          severity: 'error',
          summary: 'Sign-in failed',
          detail: this.errorMessage(error),
          life: 5000
        });
      }
    });
  }

  showPasswordHelp(): void {
    this.auth.requestPasswordResetHelp().subscribe({
      next: (response) => this.messages.add({
        severity: 'info', summary: 'Password help', detail: response.message, life: 6000
      }),
      error: (error: unknown) => this.messages.add({
        severity: 'error', summary: 'Password help unavailable',
        detail: error instanceof Error ? error.message : 'Contact your workspace administrator.'
      })
    });
  }

  private errorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const body = (error as { error?: { message?: string } }).error;
      if (body?.message) return body.message;
    }
    return error instanceof Error ? error.message : 'Check your email and password, then try again.';
  }
}