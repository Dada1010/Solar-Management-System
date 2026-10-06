import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, LoginResult, MessageResult } from '../models/api.models';
import { unwrap } from './api-envelope';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly session = signal<LoginResult | null>(null);

  constructor(private readonly http: HttpClient) {}

  get token(): string | null {
    return this.session()?.token ?? null;
  }

  get user(): LoginResult | null {
    return this.session();
  }

  get isAuthenticated(): boolean {
    return this.token !== null;
  }

  get mustChangePassword(): boolean {
    return this.session()?.mustChangePassword ?? false;
  }

  login(emailAddress: string, password: string): Observable<LoginResult> {
    return this.http.post<ApiResponse<LoginResult>>(`${environment.apiBaseUrl}/auth/login`, {
      emailAddress,
      password
    }).pipe(
      map(unwrap),
      tap((result) => this.session.set(result))
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<MessageResult> {
    return this.http.post<ApiResponse<MessageResult>>(`${environment.apiBaseUrl}/auth/change-password`, {
      currentPassword,
      newPassword
    }).pipe(
      map(unwrap),
      tap(() => {
        const currentSession = this.session();
        if (currentSession) {
          this.session.set({ ...currentSession, mustChangePassword: false });
        }
      })
    );
  }

  requestPasswordResetHelp(): Observable<MessageResult> {
    return this.http.post<ApiResponse<MessageResult>>(
      `${environment.apiBaseUrl}/auth/password-reset-help`, {}
    ).pipe(map(unwrap));
  }

  clearSession(): void {
    this.session.set(null);
  }
}