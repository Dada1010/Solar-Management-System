import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
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
  private readonly formBuilder = inject(FormBuilder);
  rows: BranchRecord[] = [];
  companies: CompanyRecord[] = [];
  loading = false;
  saving = false;
  dialogVisible = false;
  editingId: number | null = null;
  page = 0;
  size = 10;
  totalRecords = 0;
  nameFilter = '';
  private companiesLoaded = false;
  readonly form = this.formBuilder.group({
    name: this.formBuilder.nonNullable.control('', [Validators.required, Validators.maxLength(160)]),
    address: this.formBuilder.nonNullable.control(''),
    mobileNo: this.formBuilder.nonNullable.control(''),
    emailAddress: this.formBuilder.nonNullable.control('', Validators.email),
    companyId: this.formBuilder.control<number | null>(null, Validators.required)
  });

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

  get selectedCompanyName(): string {
    const id = this.form.controls.companyId.value;
    return this.companies.find((company) => company.id === id)?.name ?? this.auth.user?.companyName ?? '';
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
    this.form.reset({
      name: '', address: '', mobileNo: '', emailAddress: '',
      companyId: this.auth.user?.companyId ?? this.companies[0]?.id ?? null
    });
    this.dialogVisible = true;
  }

  edit(branch: BranchRecord): void {
    this.loading = true;
    this.api.branch(branch.id).subscribe({
      next: (record) => {
        this.editingId = record.id;
        this.form.patchValue({
          name: record.name,
          address: record.address ?? '',
          mobileNo: record.mobileNo ?? '',
          emailAddress: record.emailAddress ?? '',
          companyId: record.companyId
        });
        this.loading = false;
        this.dialogVisible = true;
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

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const payload: BranchPayload = {
      ...this.form.getRawValue(),
      companyId: Number(this.form.controls.companyId.value)
    };
    const request = this.editingId === null
      ? this.api.createBranch(payload)
      : this.api.updateBranch(this.editingId, payload);
    request.subscribe({
      next: () => {
        this.saving = false;
        this.dialogVisible = false;
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