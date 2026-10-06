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
	reverseDialogVisible = false;
	reversingPayment = false;
	pendingReversal: InvoicePaymentRecord | null = null;

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {
		this.load();
	}

	get canCancelPayments(): boolean {
		return this.auth.user?.role === 'ADMIN';
	}

	cancelPayment(payment: InvoicePaymentRecord): void {
		if (!this.canCancelPayments || payment.entryType !== 'PAYMENT' || payment.reversed || payment.invoiceStatus !== 'OPEN') return;
		this.pendingReversal = payment;
		this.reverseDialogVisible = true;
	}

	confirmPaymentReversal(): void {
		const payment = this.pendingReversal;
		if (!this.canCancelPayments || !payment || payment.invoiceStatus !== 'OPEN') return;
		this.reversingPayment = true;
		this.api.cancelInvoicePayment(payment.invoiceId, payment.id).subscribe({
			next: () => {
				this.reversingPayment = false;
				this.dismissPaymentReversal();
				this.load();
				this.messages.add({ severity: 'success', summary: 'Payment reversed', detail: 'A reversal entry was added.' });
			},
			error: (error: unknown) => {
				this.reversingPayment = false;
				this.messages.add({ severity: 'error', summary: 'Could not reverse payment',
					detail: this.errorMessage(error) });
			}
		});
	}

	dismissPaymentReversal(): void {
		if (this.reversingPayment) return;
		this.reverseDialogVisible = false;
		this.pendingReversal = null;
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