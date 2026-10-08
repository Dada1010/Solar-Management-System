import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { OtherChargeReasonRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-other-charges',
	templateUrl: './other-charges.component.html',
	styleUrl: './other-charges.component.scss',
	standalone: false
})
export class OtherChargesComponent {
	rows: OtherChargeReasonRecord[] = [];
	loading = false;
	saving = false;
	name = '';
	editingId: number | null = null;

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {
		this.load();
	}

	get canDelete(): boolean {
		return this.auth.user?.role === 'ADMIN';
	}

	edit(reason: OtherChargeReasonRecord): void {
		this.editingId = reason.id;
		this.name = reason.name;
	}

	reset(): void {
		this.editingId = null;
		this.name = '';
	}

	save(): void {
		const name = this.name.trim();
		if (!name || this.saving) return;
		this.saving = true;
		const request = this.editingId === null
			? this.api.addOtherChargeReason(name)
			: this.api.updateOtherChargeReason(this.editingId, name);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.reset();
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Reason saved.' });
				this.load();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not save reason', error);
			}
		});
	}

	remove(reason: OtherChargeReasonRecord): void {
		if (!this.canDelete || !window.confirm(`Delete the reason "${reason.name}"?`)) return;
		this.api.deleteOtherChargeReason(reason.id).subscribe({
			next: () => {
				if (this.editingId === reason.id) this.reset();
				this.messages.add({ severity: 'success', summary: 'Deleted', detail: 'Reason deleted.' });
				this.load();
			},
			error: (error: unknown) => this.showError('Could not delete reason', error)
		});
	}

	private load(): void {
		this.loading = true;
		this.api.otherChargeReasons().subscribe({
			next: (reasons) => {
				this.rows = reasons;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				this.showError('Could not load reasons', error);
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
