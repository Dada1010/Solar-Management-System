import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { MsedclDetailRecord } from '../../core/models/api.models';
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
	nameFilter = '';
	mobileNoFilter = '';
	consumerNoFilter = '';

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService) {
		this.load();
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
}