import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { WorkspaceShellComponent } from '../layout/workspace-shell/workspace-shell.component';
import { BranchComponent } from '../features/branch/branch.component';
import { CompanyComponent } from '../features/company/company.component';
import { PeopleComponent } from '../features/people/people.component';
import { ConsumerDetailsComponent } from '../features/consumer-details/consumer-details.component';
import { InvoiceComponent } from '../features/invoice/invoice.component';
import { PaymentDetailsComponent } from '../features/payment-details/payment-details.component';

@NgModule({
  declarations: [
    WorkspaceShellComponent,
    CompanyComponent,
    BranchComponent,
    PeopleComponent,
    ConsumerDetailsComponent,
    InvoiceComponent,
    PaymentDetailsComponent
  ],
  imports: [SharedModule, RouterModule],
  exports: [WorkspaceShellComponent]
})
export class WorkspaceModule {}