import { Component, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-workspace-shell',
  templateUrl: './workspace-shell.component.html',
  styleUrl: './workspace-shell.component.scss',
  standalone: false
})
export class WorkspaceShellComponent implements OnDestroy {
  activeTitle = 'Dashboard';
  navOpen = false;
  readonly profileMenu: MenuItem[] = [
    { label: 'Signed in', disabled: true },
    { separator: true },
    { label: 'Sign out', icon: 'pi pi-sign-out', command: () => this.signOut() }
  ];
  private readonly routeSubscription: Subscription;

  constructor(private readonly router: Router, readonly auth: AuthService) {
    this.setTitle(router.url);
    this.routeSubscription = router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event) => {
      this.setTitle(event.urlAfterRedirects);
      this.navOpen = false;
    });
  }

  get userInitials(): string {
    return (this.auth.user?.name ?? 'User').split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase();
  }

  signOut(): void {
    this.auth.clearSession();
    void this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.routeSubscription.unsubscribe();
  }

  private setTitle(url: string): void {
    const path = url.split('?')[0].replace(/^\//, '').split('/')[0];
    const titles: Record<string, string> = {
      dashboard: 'Dashboard',
      companies: 'Companies',
      branches: 'Branches',
      employees: 'Employees',
      customers: 'Customers',
      'consumer-details': 'Consumer Details',
      invoices: 'Invoices',
      'payment-details': 'Payment Details'
    };
    this.activeTitle = titles[path] ?? 'Dashboard';
  }
}