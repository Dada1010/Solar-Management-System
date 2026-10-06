import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { EmployeeRole } from '../models/api.models';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
	constructor(private readonly auth: AuthService, private readonly router: Router) {}

	canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
		const allowedRoles = route.data['roles'] as EmployeeRole[] | undefined;
		if (!allowedRoles || (this.auth.user && allowedRoles.includes(this.auth.user.role))) return true;
		return this.router.parseUrl('/dashboard');
	}
}
