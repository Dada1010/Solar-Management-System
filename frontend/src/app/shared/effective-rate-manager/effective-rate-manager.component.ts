import { Component, Input, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import {
	EffectiveRateOwnerType,
	EffectiveRatePayload,
	EffectiveRateRecord
} from '../../core/models/api.models';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-effective-rate-manager',
	templateUrl: './effective-rate-manager.component.html',
	styleUrl: './effective-rate-manager.component.scss',
	standalone: false
})
export class EffectiveRateManagerComponent {
	@Input({ required: true }) ownerType!: EffectiveRateOwnerType;
	@Input({ required: true }) ownerId!: number;
	@Input() ownerName = '';

	private readonly formBuilder = inject(FormBuilder);
	rates: EffectiveRateRecord[] = [];
	dialogVisible = false;
	loading = false;
	saving = false;
	editingId: number | null = null;
	readonly form = this.formBuilder.nonNullable.group({
		startDate: ['', Validators.required],
		ratePerUnit: [0, [Validators.required, Validators.min(0)]]
	});

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService) {}

	open(): void {
		this.dialogVisible = true;
		this.resetForm();
		this.loadRates();
	}

	edit(rate: EffectiveRateRecord): void {
		this.editingId = rate.id;
		this.form.patchValue({ startDate: rate.startDate, ratePerUnit: rate.ratePerUnit });
	}

	resetForm(): void {
		this.editingId = null;
		this.form.reset({ startDate: '', ratePerUnit: 0 });
	}

	save(): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		this.saving = true;
		const payload: EffectiveRatePayload = this.form.getRawValue();
		const request = this.editingId === null
			? this.api.addEffectiveRate(this.ownerType, this.ownerId, payload)
			: this.api.updateEffectiveRate(this.ownerType, this.ownerId, this.editingId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.resetForm();
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Effective rate saved.' });
				this.loadRates();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not save effective rate', error);
			}
		});
	}

	remove(rate: EffectiveRateRecord): void {
		if (!window.confirm(`Remove the rate starting ${rate.startDate}?`)) return;
		this.api.deleteEffectiveRate(this.ownerType, this.ownerId, rate.id).subscribe({
			next: () => {
				if (this.editingId === rate.id) this.resetForm();
				this.messages.add({ severity: 'success', summary: 'Removed', detail: 'Effective rate removed.' });
				this.loadRates();
			},
			error: (error: unknown) => this.showError('Could not remove effective rate', error)
		});
	}

	private loadRates(): void {
		this.loading = true;
		this.api.effectiveRates(this.ownerType, this.ownerId).subscribe({
			next: (rates) => {
				this.rates = rates;
				this.loading = false;
			},
			error: (error: unknown) => {
				this.loading = false;
				this.showError('Could not load effective rates', error);
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