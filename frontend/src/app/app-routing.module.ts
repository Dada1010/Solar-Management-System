import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { FirstLoginGuard } from './core/guards/first-login.guard';
import { FirstLoginComponent } from './auth/first-login/first-login.component';
import { LoginComponent } from './auth/login/login.component';
import { BranchComponent } from './features/branch/branch.component';
import { CompanyComponent } from './features/company/company.component';
import { ConsumerDetailsComponent } from './features/consumer-details/consumer-details.component';
import { InvoiceComponent } from './features/invoice/invoice.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { PaymentDetailsComponent } from './features/payment-details/payment-details.component';
import { PeopleComponent } from './features/people/people.component';
import { WorkspaceShellComponent } from './layout/workspace-shell/workspace-shell.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'first-login', component: FirstLoginComponent, canActivate: [FirstLoginGuard] },
  {
    path: '',
    component: WorkspaceShellComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'companies', component: CompanyComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'USER'] } },
      { path: 'branches', component: BranchComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'USER'] } },
      { path: 'employees', component: PeopleComponent, canActivate: [RoleGuard], data: { employeeType: 'COMPANY_EMPLOYEE', roles: ['ADMIN', 'USER'] } },
      { path: 'customers', component: PeopleComponent, canActivate: [RoleGuard], data: { employeeType: 'CUSTOMER', roles: ['ADMIN', 'USER'] } },
      { path: 'consumer-details', component: ConsumerDetailsComponent },
      { path: 'invoices', component: InvoiceComponent },
      { path: 'payment-details', component: PaymentDetailsComponent },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}