import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { InvoicePaymentRecord } from '../../core/models/api.models';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-payment-details',
	templateUrl: './payment-details.component.html',
	styleUrl: './payment-details.component.scss',
	standalone: false
})
export class PaymentDetailsComponent {
	rows: InvoicePaymentRecord[] = [];
	loading = false;

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService) {
		this.load();
	}

	private load(): void {
		this.loading = true;
		this.api.paymentDetails().subscribe({
			next: (payments) => {
				this.rows = payments;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				const responseMessage = typeof error === 'object' && error !== null && 'error' in error
					? (error as { error?: { message?: string } }).error?.message
					: undefined;
				this.messages.add({ severity: 'error', summary: 'Could not load payment details',
					detail: responseMessage ?? 'Please try again.' });
			}
		});
	}
}