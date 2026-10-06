import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { CompanyPayload, CompanyRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-company',
	templateUrl: './company.component.html',
	styleUrl: './company.component.scss',
	standalone: false
})
export class CompanyComponent {
	private readonly formBuilder = inject(FormBuilder);
	rows: CompanyRecord[] = [];
	loading = false;
	saving = false;
	dialogVisible = false;
	editingId: number | null = null;
	page = 0;
	size = 10;
	totalRecords = 0;
	readonly form = this.formBuilder.nonNullable.group({
		name: ['', [Validators.required, Validators.maxLength(160)]],
		industry: ['', Validators.maxLength(120)],
		address: ['', Validators.maxLength(255)],
		mobileNo: [''],
		emailAddress: ['', Validators.email]
	});

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {
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
		this.api.companies(this.page, this.size).subscribe({
			next: (result) => {
				this.rows = result.items;
				this.totalRecords = result.totalElements;
				this.loading = false;
			},
			error: (error: unknown) => this.handleError('Could not load companies', error)
		});
	}

	changePage(event: { page?: number | null; rows?: number | null }): void {
		this.page = event.page ?? 0;
		this.size = event.rows ?? this.size;
		this.loadPage();
	}

	create(): void {
		this.editingId = null;
		this.form.reset({ name: '', industry: '', address: '', mobileNo: '', emailAddress: '' });
		this.dialogVisible = true;
	}

	edit(company: CompanyRecord): void {
		this.loading = true;
		this.api.company(company.id).subscribe({
			next: (record) => {
				this.editingId = record.id;
				this.form.patchValue({
					name: record.name,
					industry: record.industry ?? '',
					address: record.address ?? '',
					mobileNo: record.mobileNo ?? '',
					emailAddress: record.emailAddress ?? ''
				});
				this.loading = false;
				this.dialogVisible = true;
			},
			error: (error: unknown) => this.handleError('Could not load company', error)
		});
	}

	remove(company: CompanyRecord): void {
		if (!this.canDelete || !window.confirm(`Delete company ${company.name}?`)) return;
		this.api.deleteCompany(company.id).subscribe({
			next: () => {
				this.messages.add({ severity: 'success', summary: 'Deleted', detail: 'Company deleted.' });
				this.loadPage();
			},
			error: (error: unknown) => this.handleError('Could not delete company', error)
		});
	}

	save(): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		this.saving = true;
		const payload: CompanyPayload = this.form.getRawValue();
		const request = this.editingId === null
			? this.api.createCompany(payload)
			: this.api.updateCompany(this.editingId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.dialogVisible = false;
				this.page = 0;
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Company saved successfully.' });
				this.loadPage();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.handleError('Could not save company', error);
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