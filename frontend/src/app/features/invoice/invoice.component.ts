import { Component, ElementRef, HostListener, OnDestroy, ViewChild, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MenuItem, MessageService } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { forkJoin, Subscription } from 'rxjs';
import { InvoicePaymentPayload, InvoicePaymentRecord, InvoicePaymentType, MsedclDetailRecord, MsedclInvoicePayload, MsedclInvoiceRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-invoice',
	templateUrl: './invoice.component.html',
	styleUrl: './invoice.component.scss',
	standalone: false
})
export class InvoiceComponent implements OnDestroy {
	private readonly formBuilder = inject(FormBuilder);
	details: MsedclDetailRecord[] = [];
	invoices: MsedclInvoiceRecord[] = [];
	previewResult: MsedclInvoiceRecord | null = null;
	selectedPaymentInvoice: MsedclInvoiceRecord | null = null;
	paymentHistory: InvoicePaymentRecord[] = [];
	invoiceActionItems: MenuItem[] = [];
	loading = false;
	previewing = false;
	saving = false;
	loadingPayments = false;
	savingPayment = false;
	paymentDialogVisible = false;
	paymentEntryVisible = false;
	billPreviewVisible = false;
	billPreviewUrl: SafeResourceUrl | null = null;
	formVisible = false;
	printRecord: MsedclInvoiceRecord | null = null;
	uploadingInvoiceId: number | null = null;
	private uploadTargetInvoice: MsedclInvoiceRecord | null = null;
	private billObjectUrl: string | null = null;
	private readonly sanitizer = inject(DomSanitizer);
	@ViewChild('msebBillPicker') private msebBillPicker?: ElementRef<HTMLInputElement>;
	readonly form = this.formBuilder.group({
		msedclDetailId: this.formBuilder.control<number | null>(null, Validators.required),
		invoiceDate: this.formBuilder.nonNullable.control(this.today(), Validators.required),
		billingDate: this.formBuilder.nonNullable.control(this.today(), Validators.required),
		importCurrent: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		importPrevious: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		exportCurrent: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		exportPrevious: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		generationCurrent: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		generationPrevious: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		previousBankUnits: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)]),
		msebBillAmount: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(0)])
	});
	readonly paymentForm = this.formBuilder.nonNullable.group({
		paymentDate: [this.today(), Validators.required],
		amount: [0, [Validators.required, Validators.min(0.01)]],
		paymentType: this.formBuilder.nonNullable.control<InvoicePaymentType>('CASH', Validators.required),
		transactionNo: [''],
		note: ['', Validators.maxLength(255)]
	});
	private readonly formSubscription: Subscription;

	constructor(
		private readonly api: ManagementApiService,
		private readonly messages: MessageService,
		readonly auth: AuthService
	) {
		this.formSubscription = this.form.valueChanges.subscribe(() => this.previewResult = null);
		this.load();
	}

	get canCreate(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
	}

	get canRecordPayment(): boolean {
		return this.canCreate;
	}

	get isUpiPayment(): boolean {
		return this.paymentForm.controls.paymentType.value === 'UPI';
	}

	get selectedDetail(): MsedclDetailRecord | undefined {
		const id = this.form.controls.msedclDetailId.value;
		return this.details.find((detail) => detail.id === id);
	}

	get showMsebBillAmount(): boolean {
		return this.selectedDetail?.chargeType === 'SOLAR_PLUS_MSEB_BILL_AMOUNT';
	}

	get importConsumption(): number {
		return this.consumption(this.form.controls.importCurrent.value, this.form.controls.importPrevious.value);
	}

	get exportConsumption(): number {
		return this.consumption(this.form.controls.exportCurrent.value, this.form.controls.exportPrevious.value);
	}

	get generationConsumption(): number {
		return this.consumption(this.form.controls.generationCurrent.value, this.form.controls.generationPrevious.value);
	}

	ngOnDestroy(): void {
		this.formSubscription.unsubscribe();
		this.revokeBillPreview();
	}

	onPaymentTypeChange(): void {
		const transactionNo = this.paymentForm.controls.transactionNo;
		if (this.isUpiPayment) {
			transactionNo.setValidators([Validators.required, Validators.maxLength(100)]);
		} else {
			transactionNo.clearValidators();
			transactionNo.setValue('');
		}
		transactionNo.updateValueAndValidity();
	}

	@HostListener('window:afterprint')
	clearPrintRecord(): void {
		this.printRecord = null;
	}

	openCreateForm(): void {
		this.previewResult = null;
		this.form.reset({
			msedclDetailId: this.details[0]?.id ?? null,
			invoiceDate: this.today(),
			billingDate: this.today(),
			importCurrent: 0,
			importPrevious: 0,
			exportCurrent: 0,
			exportPrevious: 0,
			generationCurrent: 0,
			generationPrevious: 0,
			previousBankUnits: 0,
			msebBillAmount: 0
		});
		this.formVisible = true;
	}

	cancelCreateForm(): void {
		this.formVisible = false;
		this.previewResult = null;
	}

	preview(): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		this.previewing = true;
		this.api.previewInvoice(this.payload()).subscribe({
			next: (result) => {
				this.previewResult = result;
				this.previewing = false;
			},
			error: (error: unknown) => {
				this.previewing = false;
				this.showError('Could not calculate invoice', error);
			}
		});
	}

	printInvoice(invoice: MsedclInvoiceRecord): void {
		this.printRecord = invoice;
		setTimeout(() => window.print(), 0);
	}

	uploadMsebBill(invoice: MsedclInvoiceRecord, event: Event): void {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file || invoice.id === null) return;
		this.uploadingInvoiceId = invoice.id;
		this.api.uploadInvoiceMsebBill(invoice.id, file).subscribe({
			next: (updatedInvoice) => {
				this.invoices = this.invoices.map((item) => item.id === updatedInvoice.id ? updatedInvoice : item);
				this.uploadingInvoiceId = null;
				input.value = '';
				this.messages.add({ severity: 'success', summary: 'Uploaded', detail: 'MSEB bill attached to invoice.' });
			},
			error: (error: unknown) => {
				this.uploadingInvoiceId = null;
				input.value = '';
				this.showError('Could not upload MSEB bill', error);
			}
		});
	}

	downloadMsebBill(invoice: MsedclInvoiceRecord): void {
		if (invoice.id === null) return;
		this.api.downloadInvoiceMsebBill(invoice.id).subscribe({
			next: (response) => {
				if (!response.body) return;
				const url = URL.createObjectURL(response.body);
				const anchor = document.createElement('a');
				anchor.href = url;
				anchor.download = invoice.msebBillFileName ?? 'mseb-bill';
				anchor.click();
				URL.revokeObjectURL(url);
			},
			error: (error: unknown) => this.showError('Could not download MSEB bill', error)
		});
	}

	viewMsebBill(invoice: MsedclInvoiceRecord): void {
		if (invoice.id === null) return;
		this.api.downloadInvoiceMsebBill(invoice.id).subscribe({
			next: (response) => {
				if (!response.body) return;
				this.revokeBillPreview();
				this.billObjectUrl = URL.createObjectURL(response.body);
				this.billPreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.billObjectUrl);
				this.billPreviewVisible = true;
			},
			error: (error: unknown) => this.showError('Could not view MSEB bill', error)
		});
	}

	closeBillPreview(): void {
		this.billPreviewVisible = false;
		this.revokeBillPreview();
	}

	openInvoiceActions(invoice: MsedclInvoiceRecord, event: Event, menu: Menu): void {
		this.invoiceActionItems = [
			{ label: 'Print invoice', icon: 'pi pi-print', command: () => this.printInvoice(invoice) }
		];
		if (invoice.chargeType === 'SOLAR_PLUS_MSEB_BILL_AMOUNT') {
			this.invoiceActionItems.push(invoice.msebBillFileName
				? { label: 'View uploaded MSEB bill', icon: 'pi pi-eye', command: () => this.viewMsebBill(invoice) }
				: { label: 'Upload MSEB bill', icon: 'pi pi-upload', disabled: !this.canCreate, command: () => this.openMsebBillPicker(invoice) });
		}
		this.invoiceActionItems.push({ label: 'Payment details', icon: 'pi pi-list', command: () => this.openPaymentHistory(invoice) });
		if (this.canRecordPayment && invoice.balanceAmount > 0) {
			this.invoiceActionItems.push({ label: 'Add payment', icon: 'pi pi-plus-circle', command: () => this.openPaymentHistory(invoice, true) });
		}
		menu.toggle(event);
	}

	openMsebBillPicker(invoice: MsedclInvoiceRecord): void {
		this.uploadTargetInvoice = invoice;
		this.msebBillPicker?.nativeElement.click();
	}

	uploadSelectedMsebBill(event: Event): void {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		const invoice = this.uploadTargetInvoice;
		this.uploadTargetInvoice = null;
		if (!file || !invoice || invoice.id === null) return;
		this.uploadingInvoiceId = invoice.id;
		this.api.uploadInvoiceMsebBill(invoice.id, file).subscribe({
			next: (updatedInvoice) => {
				this.invoices = this.invoices.map((item) => item.id === updatedInvoice.id ? updatedInvoice : item);
				this.uploadingInvoiceId = null;
				input.value = '';
				this.messages.add({ severity: 'success', summary: 'Uploaded', detail: 'MSEB bill attached to invoice.' });
			},
			error: (error: unknown) => {
				this.uploadingInvoiceId = null;
				input.value = '';
				this.showError('Could not upload MSEB bill', error);
			}
		});
	}

	openPaymentHistory(invoice: MsedclInvoiceRecord, openEntry = false): void {
		if (invoice.id === null) return;
		this.selectedPaymentInvoice = invoice;
		this.paymentHistory = [];
		this.paymentForm.reset({ paymentDate: this.today(), amount: 0, paymentType: 'CASH', transactionNo: '', note: '' });
		this.paymentEntryVisible = openEntry;
		this.onPaymentTypeChange();
		this.paymentDialogVisible = true;
		this.loadingPayments = true;
		this.api.invoicePayments(invoice.id).subscribe({
			next: (payments) => {
				this.paymentHistory = payments;
				this.loadingPayments = false;
			},
			error: (error: unknown) => {
				this.loadingPayments = false;
				this.showError('Could not load payment history', error);
			}
		});
	}

	addPayment(): void {
		if (!this.selectedPaymentInvoice || this.selectedPaymentInvoice.id === null || this.paymentForm.invalid) {
			this.paymentForm.markAllAsTouched();
			return;
		}
		const value = this.paymentForm.getRawValue();
		if (value.amount > this.selectedPaymentInvoice.balanceAmount) {
			this.messages.add({ severity: 'error', summary: 'Payment exceeds balance', detail: 'Enter an amount no greater than the outstanding balance.' });
			return;
		}
		this.savingPayment = true;
		const payload: InvoicePaymentPayload = value;
		this.api.addInvoicePayment(this.selectedPaymentInvoice.id, payload).subscribe({
			next: (payments) => {
				this.paymentHistory = payments;
				this.savingPayment = false;
				this.paymentForm.reset({ paymentDate: this.today(), amount: 0, paymentType: 'CASH', transactionNo: '', note: '' });
				this.paymentEntryVisible = false;
				this.loadInvoices();
				this.messages.add({ severity: 'success', summary: 'Payment recorded', detail: 'Invoice balance updated.' });
			},
			error: (error: unknown) => {
				this.savingPayment = false;
				this.showError('Could not record payment', error);
			}
		});
	}

	save(): void {
		if (!this.previewResult || this.form.invalid) return;
		this.saving = true;
		this.api.createInvoice(this.payload()).subscribe({
			next: (invoice) => {
				this.previewResult = invoice;
				this.saving = false;
				this.formVisible = false;
				this.invoices = [invoice, ...this.invoices];
				this.messages.add({ severity: 'success', summary: 'Invoice saved', detail: `Invoice total: ${invoice.invoiceAmount.toFixed(2)}` });
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not save invoice', error);
			}
		});
	}

	private load(): void {
		this.loading = true;
		forkJoin({ details: this.api.consumerDetails(), invoices: this.api.invoices() }).subscribe({
			next: (result) => {
				this.details = result.details;
				this.invoices = result.invoices;
				if (this.details.length) this.form.controls.msedclDetailId.setValue(this.details[0].id);
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				this.showError('Could not load invoices', error);
			}
		});
	}

	private loadInvoices(): void {
		this.api.invoices().subscribe({
			next: (invoices) => {
				this.invoices = invoices;
				if (this.selectedPaymentInvoice) {
					this.selectedPaymentInvoice = invoices.find((invoice) => invoice.id === this.selectedPaymentInvoice?.id)
						?? this.selectedPaymentInvoice;
				}
			},
		error: (error: unknown) => this.showError('Could not refresh invoices', error)
		});
	}

	private payload(): MsedclInvoicePayload {
		const value = this.form.getRawValue();
		return {
			msedclDetailId: Number(value.msedclDetailId),
			invoiceDate: value.invoiceDate,
			billingDate: value.billingDate,
			importCurrent: value.importCurrent,
			importPrevious: value.importPrevious,
			exportCurrent: value.exportCurrent,
			exportPrevious: value.exportPrevious,
			generationCurrent: value.generationCurrent,
			generationPrevious: value.generationPrevious,
			previousBankUnits: value.previousBankUnits,
			msebBillAmount: this.showMsebBillAmount ? value.msebBillAmount : null
		};
	}

	private consumption(current: number, previous: number): number {
		return Math.max(0, current - previous);
	}

	private today(): string {
		const now = new Date();
		const month = String(now.getMonth() + 1).padStart(2, '0');
		const day = String(now.getDate()).padStart(2, '0');
		return `${now.getFullYear()}-${month}-${day}`;
	}

	private showError(summary: string, error: unknown): void {
		const responseMessage = typeof error === 'object' && error !== null && 'error' in error
			? (error as { error?: { message?: string } }).error?.message
			: undefined;
		this.messages.add({ severity: 'error', summary, detail: responseMessage ?? 'Please try again.' });
	}

	private revokeBillPreview(): void {
		if (this.billObjectUrl) URL.revokeObjectURL(this.billObjectUrl);
		this.billObjectUrl = null;
		this.billPreviewUrl = null;
	}
}