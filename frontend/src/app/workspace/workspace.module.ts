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
import { DashboardComponent } from '../features/dashboard/dashboard.component';
import { CompanyEditorComponent } from '../features/company/company-editor/company-editor.component';
import { BranchEditorComponent } from '../features/branch/branch-editor/branch-editor.component';
import { PeopleEditorComponent } from '../features/people/people-editor/people-editor.component';
import { MsedclDetailEditorComponent } from '../features/people/msedcl-detail-editor/msedcl-detail-editor.component';

@NgModule({
  declarations: [
    WorkspaceShellComponent,
    DashboardComponent,
    CompanyComponent,
    CompanyEditorComponent,
    BranchComponent,
    BranchEditorComponent,
    PeopleComponent,
    PeopleEditorComponent,
    MsedclDetailEditorComponent,
    ConsumerDetailsComponent,
    InvoiceComponent,
    PaymentDetailsComponent
  ],
  imports: [SharedModule, RouterModule],
  exports: [WorkspaceShellComponent]
})
export class WorkspaceModule {}