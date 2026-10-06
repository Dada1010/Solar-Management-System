import { Component } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { Observable, forkJoin, map, of } from 'rxjs';
import { BranchPayload, BranchRecord, CompanyRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
  selector: 'app-branch',
  templateUrl: './branch.component.html',
  styleUrl: './branch.component.scss',
  standalone: false
})
export class BranchComponent {
  rows: BranchRecord[] = [];
  companies: CompanyRecord[] = [];
  loading = false;
  saving = false;
  editorVisible = false;
  editingId: number | null = null;
  editingBranch: BranchRecord | null = null;
  branchActionItems: MenuItem[] = [];
  page = 0;
  size = 10;
  totalRecords = 0;
  nameFilter = '';
  private companiesLoaded = false;
  constructor(
    private readonly api: ManagementApiService,
    readonly auth: AuthService,
    private readonly messages: MessageService
  ) {
    if (auth.user?.companyId) {
      this.companies = [{
        id: auth.user.companyId,
        name: auth.user.companyName,
        industry: null,
        address: null,
        mobileNo: null,
        emailAddress: null,
        branchCount: 0
      }];
      this.companiesLoaded = true;
    }
    this.loadPage();
  }

  get canWrite(): boolean {
    return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
  }

  get canDelete(): boolean {
    return this.auth.user?.role === 'ADMIN';
  }

  loadPage(): void {
    this.loading = true;
    const companyRequest: Observable<CompanyRecord[]> = this.companiesLoaded
      ? of(this.companies)
      : this.api.companies(0, 100).pipe(map((result) => result.items));
    forkJoin({
      page: this.api.branches(this.page, this.size, this.nameFilter),
      companies: companyRequest
    }).subscribe({
      next: (result) => {
        this.rows = result.page.items;
        this.totalRecords = result.page.totalElements;
        this.companies = result.companies;
        this.companiesLoaded = true;
        this.loading = false;
      },
      error: (error: unknown) => this.handleError('Could not load branches', error)
    });
  }

  applyFilter(): void {
    this.page = 0;
    this.loadPage();
  }

  clearFilter(): void {
    this.nameFilter = '';
    this.applyFilter();
  }

  changePage(event: { page?: number | null; rows?: number | null }): void {
    this.page = event.page ?? 0;
    this.size = event.rows ?? this.size;
    this.loadPage();
  }

  create(): void {
    this.editingId = null;
    this.editingBranch = null;
    this.editorVisible = true;
  }

  edit(branch: BranchRecord): void {
    this.loading = true;
    this.api.branch(branch.id).subscribe({
      next: (record) => {
        this.editingId = record.id;
        this.editingBranch = record;
        this.loading = false;
        this.editorVisible = true;
      },
      error: (error: unknown) => this.handleError('Could not load branch', error)
    });
  }

  remove(branch: BranchRecord): void {
    if (!this.canDelete || !window.confirm(`Delete branch ${branch.name}?`)) return;
    this.api.deleteBranch(branch.id).subscribe({
      next: () => {
        this.messages.add({ severity: 'success', summary: 'Deleted', detail: 'Branch deleted.' });
        this.loadPage();
      },
      error: (error: unknown) => this.handleError('Could not delete branch', error)
    });
  }

  openActions(branch: BranchRecord, event: Event, menu: Menu): void {
    this.branchActionItems = [
      { label: 'Update', icon: 'pi pi-pencil', command: () => this.edit(branch) }
    ];
    if (this.canDelete) {
      this.branchActionItems.push({ label: 'Delete', icon: 'pi pi-trash', command: () => this.remove(branch) });
    }
    menu.toggle(event);
  }

  saveBranch(payload: BranchPayload): void {
    this.saving = true;
    const request = this.editingId === null
      ? this.api.createBranch(payload)
      : this.api.updateBranch(this.editingId, payload);
    request.subscribe({
      next: () => {
        this.saving = false;
        this.editorVisible = false;
        this.editingBranch = null;
        this.page = 0;
        this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Branch saved successfully.' });
        this.loadPage();
      },
      error: (error: unknown) => {
        this.saving = false;
        this.handleError('Could not save branch', error);
      }
    });
  }

  private handleError(summary: string, error: unknown): void {
    this.loading = false;
    const responseMessage = typeof error === 'object' && error !== null && 'error' in error
      ? (error as { error?: { message?: string } }).error?.message
      : undefined;
    this.messages.add({ severity: 'error', summary, detail: responseMessage ?? 'Please try again.' });
  }
}