import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ReferralIncentiveReportRow } from '../../core/models/api.models';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-referral-report',
	templateUrl: './referral-report.component.html',
	styleUrl: './referral-report.component.scss',
	standalone: false
})
export class ReferralReportComponent {
	rows: ReferralIncentiveReportRow[] = [];
	selectedYear = '';
	loading = false;

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService) {
		this.load();
	}

	get years(): string[] {
		return [...new Set(this.rows.map((row) => row.month.slice(0, 4)))].sort((a, b) => b.localeCompare(a));
	}

	get filteredRows(): ReferralIncentiveReportRow[] {
		return this.selectedYear
			? this.rows.filter((row) => row.month.startsWith(`${this.selectedYear}-`))
			: this.rows;
	}

	get incentiveTotal(): number {
		return this.filteredRows.reduce((total, row) => total + row.incentiveAmount, 0);
	}

	get solarTotal(): number {
		return this.filteredRows.reduce((total, row) => total + row.solarAmount, 0);
	}

	get invoiceTotal(): number {
		return this.filteredRows.reduce((total, row) => total + row.invoiceCount, 0);
	}

	monthLabel(month: string): string {
		const [year, monthNumber] = month.split('-').map(Number);
		return new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
			.format(new Date(year, monthNumber - 1, 1));
	}

	load(): void {
		this.loading = true;
		this.api.referralIncentiveReport().subscribe({
			next: (rows) => {
				this.rows = rows;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				const responseMessage = typeof error === 'object' && error !== null && 'error' in error
					? (error as { error?: { message?: string } }).error?.message
					: undefined;
				this.messages.add({ severity: 'error', summary: 'Could not load referral report',
					detail: responseMessage ?? 'Please try again.' });
			}
		});
	}
}