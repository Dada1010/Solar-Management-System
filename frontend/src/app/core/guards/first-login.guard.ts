import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class FirstLoginGuard implements CanActivate {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  canActivate(): boolean | UrlTree {
    if (!this.auth.isAuthenticated) {
      return this.router.parseUrl('/login');
    }
    return this.auth.mustChangePassword ? true : this.router.parseUrl('/companies');
  }
}