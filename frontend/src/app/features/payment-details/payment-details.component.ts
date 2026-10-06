import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { InvoicePaymentRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
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

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {
		this.load();
	}

	get canCancelPayments(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
	}

	cancelPayment(payment: InvoicePaymentRecord): void {
		if (payment.entryType !== 'PAYMENT' || payment.reversed || payment.invoiceStatus !== 'OPEN'
			|| !window.confirm(`Add a reversal for payment ${payment.id}? The original payment will be retained.`)) return;
		this.api.cancelInvoicePayment(payment.invoiceId, payment.id).subscribe({
			next: () => {
				this.load();
				this.messages.add({ severity: 'success', summary: 'Payment reversed', detail: 'A reversal entry was added.' });
			},
			error: (error: unknown) => this.messages.add({ severity: 'error', summary: 'Could not reverse payment',
				detail: this.errorMessage(error) })
		});
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

	private errorMessage(error: unknown): string {
		const responseMessage = typeof error === 'object' && error !== null && 'error' in error
			? (error as { error?: { message?: string } }).error?.message
			: undefined;
		return responseMessage ?? 'Please try again.';
	}
}