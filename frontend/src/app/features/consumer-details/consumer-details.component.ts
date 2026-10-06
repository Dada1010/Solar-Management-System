import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { MsedclDetailPayload, MsedclDetailRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-consumer-details',
	templateUrl: './consumer-details.component.html',
	styleUrl: './consumer-details.component.scss',
	standalone: false
})
export class ConsumerDetailsComponent {
	rows: MsedclDetailRecord[] = [];
	loading = false;
	saving = false;
	editorVisible = false;
	editingDetail: MsedclDetailRecord | null = null;
	nameFilter = '';
	mobileNoFilter = '';
	consumerNoFilter = '';

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {
		this.load();
	}

	get canViewEffectiveRates(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER'
			|| this.auth.user?.role === 'CUSTOMER';
	}

	get canUpdateDetails(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
	}

	edit(detail: MsedclDetailRecord): void {
		if (!this.canUpdateDetails) return;
		this.editingDetail = detail;
		this.editorVisible = true;
	}

	saveDetail(payload: MsedclDetailPayload): void {
		if (!this.canUpdateDetails || !this.editingDetail) return;
		this.saving = true;
		this.api.updateConsumerMsedclDetail(this.editingDetail.id, payload).subscribe({
			next: (updated) => {
				this.rows = this.rows.map((item) => item.id === updated.id ? updated : item);
				this.saving = false;
				this.editorVisible = false;
				this.editingDetail = null;
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Consumer details updated.' });
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not update consumer details', error);
			}
		});
	}

	cancelEdit(): void {
		this.editorVisible = false;
		this.editingDetail = null;
	}

	applyFilters(): void {
		this.load();
	}

	clearFilters(): void {
		this.nameFilter = '';
		this.mobileNoFilter = '';
		this.consumerNoFilter = '';
		this.load();
	}

	private load(): void {
		this.loading = true;
		this.api.consumerDetails(this.nameFilter, this.mobileNoFilter, this.consumerNoFilter).subscribe({
			next: (records) => {
				this.rows = records;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				const responseMessage = typeof error === 'object' && error !== null && 'error' in error
					? (error as { error?: { message?: string } }).error?.message
					: undefined;
				this.messages.add({ severity: 'error', summary: 'Could not load consumer details',
					detail: responseMessage ?? 'Please try again.' });
			}
		});
	}

	private showError(summary: string, error: unknown): void {
		const responseMessage = typeof error === 'object' && error !== null && 'error' in error
			? (error as { error?: { message?: string } }).error?.message
			: undefined;
		this.messages.add({ severity: 'error', summary, detail: responseMessage ?? 'Please try again.' });
	}
}