import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { WorkspaceShellComponent } from '../layout/workspace-shell/workspace-shell.component';
import { BranchComponent } from '../features/branch/branch.component';
import { CompanyComponent } from '../features/company/company.component';
import { PeopleComponent } from '../features/people/people.component';

@NgModule({
  declarations: [
    WorkspaceShellComponent,
    CompanyComponent,
    BranchComponent,
    PeopleComponent
  ],
  imports: [SharedModule, RouterModule],
  exports: [WorkspaceShellComponent]
})
export class WorkspaceModule {}