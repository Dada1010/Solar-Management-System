import { Component } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { Menu } from 'primeng/menu';
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
	rows: CompanyRecord[] = [];
	loading = false;
	saving = false;
	editorVisible = false;
	editingId: number | null = null;
	editingCompany: CompanyRecord | null = null;
	companyActionItems: MenuItem[] = [];
	page = 0;
	size = 10;
	totalRecords = 0;

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
		this.editingCompany = null;
		this.editorVisible = true;
	}

	edit(company: CompanyRecord): void {
		this.loading = true;
		this.api.company(company.id).subscribe({
			next: (record) => {
				this.editingId = record.id;
				this.editingCompany = record;
				this.loading = false;
				this.editorVisible = true;
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

	openActions(company: CompanyRecord, event: Event, menu: Menu): void {
		this.companyActionItems = [
			{ label: 'Update', icon: 'pi pi-pencil', command: () => this.edit(company) }
		];
		if (this.canDelete) {
			this.companyActionItems.push({ label: 'Delete', icon: 'pi pi-trash', command: () => this.remove(company) });
		}
		menu.toggle(event);
	}

	saveCompany(payload: CompanyPayload): void {
		this.saving = true;
		const request = this.editingId === null
			? this.api.createCompany(payload)
			: this.api.updateCompany(this.editingId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.editorVisible = false;
				this.editingCompany = null;
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

	closeEditor(): void {
		this.editorVisible = false;
		this.editingCompany = null;
	}

	private handleError(summary: string, error: unknown): void {
		this.loading = false;
		const responseMessage = typeof error === 'object' && error !== null && 'error' in error
			? (error as { error?: { message?: string } }).error?.message
			: undefined;
		this.messages.add({ severity: 'error', summary, detail: responseMessage ?? 'Please try again.' });
	}
}