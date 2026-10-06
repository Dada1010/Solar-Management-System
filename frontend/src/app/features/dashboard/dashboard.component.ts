import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { DashboardRecord } from '../../core/models/api.models';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-dashboard',
	templateUrl: './dashboard.component.html',
	styleUrl: './dashboard.component.scss',
	standalone: false
})
export class DashboardComponent {
	report: DashboardRecord | null = null;
	loading = false;

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService) {
		this.load();
	}

	load(): void {
		this.loading = true;
		this.api.dashboard().subscribe({
			next: (report) => {
				this.report = report;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				const responseMessage = typeof error === 'object' && error !== null && 'error' in error
					? (error as { error?: { message?: string } }).error?.message
					: undefined;
				this.messages.add({ severity: 'error', summary: 'Could not load dashboard',
					detail: responseMessage ?? 'Please try again.' });
			}
		});
	}
}
