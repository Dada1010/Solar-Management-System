import { Component, Input, inject } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import {
	EffectiveRateOwnerType,
	EffectiveRatePayload,
	EffectiveRateRecord
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

interface SlabControls {
	upToUnits: FormControl<number | null>;
	ratePerUnit: FormControl<number>;
	adjustmentPerUnit: FormControl<number>;
}

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
		ratePerUnit: [0, [Validators.required, Validators.min(0)]],
		fixedCharge: [0, [Validators.required, Validators.min(0)]],
		wheelingChargePerUnit: [0, [Validators.required, Validators.min(0)]],
		electricityDutyPercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
		taxOnSalePaisePerUnit: [0, [Validators.required, Validators.min(0)]],
		slabs: this.formBuilder.array<FormGroup<SlabControls>>([])
	});

	constructor(private readonly api: ManagementApiService, private readonly messages: MessageService,
		readonly auth: AuthService) {}

	get canWrite(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
	}

	get canDelete(): boolean {
		return this.auth.user?.role === 'ADMIN';
	}

	open(): void {
		this.dialogVisible = true;
		this.resetForm();
		this.loadRates();
	}

	edit(rate: EffectiveRateRecord): void {
		if (!this.canWrite) return;
		this.editingId = rate.id;
		this.form.patchValue({
			startDate: rate.startDate,
			ratePerUnit: rate.ratePerUnit,
			fixedCharge: rate.fixedCharge,
			wheelingChargePerUnit: rate.wheelingChargePerUnit,
			electricityDutyPercent: rate.electricityDutyPercent,
			taxOnSalePaisePerUnit: rate.taxOnSalePaisePerUnit
		});
		this.slabRows.clear();
		rate.slabs.forEach((slab) => this.slabRows.push(this.slabGroup(slab.upToUnits, slab.ratePerUnit, slab.adjustmentPerUnit)));
	}

	resetForm(): void {
		this.editingId = null;
		this.slabRows.clear();
		this.form.reset({ startDate: '', ratePerUnit: 0 });
	}

	get slabRows(): FormArray<FormGroup<SlabControls>> {
		return this.form.controls.slabs;
	}

	addSlab(): void {
		this.slabRows.push(this.slabGroup(null, 0, 0));
	}

	removeSlab(index: number): void {
		this.slabRows.removeAt(index);
	}

	slabStart(index: number): number {
		return index === 0 ? 0 : Number(this.slabRows.at(index - 1).controls.upToUnits.value ?? 0);
	}

	slabSummary(rate: EffectiveRateRecord): string {
		let from = 0;
		return rate.slabs.map((slab) => {
			const label = slab.upToUnits === null ? `>${from}` : `${from}-${slab.upToUnits}`;
			from = slab.upToUnits ?? from;
			return `${label}: ${slab.ratePerUnit} + ${slab.adjustmentPerUnit}`;
		}).join(' | ');
	}

	private slabGroup(upToUnits: number | null, ratePerUnit: number, adjustmentPerUnit: number): FormGroup<SlabControls> {
		return this.formBuilder.group({
			upToUnits: this.formBuilder.control<number | null>(upToUnits),
			ratePerUnit: this.formBuilder.nonNullable.control(ratePerUnit, [Validators.required, Validators.min(0)]),
			adjustmentPerUnit: this.formBuilder.nonNullable.control(adjustmentPerUnit, [Validators.required, Validators.min(0)])
		});
	}

	save(): void {
		if (!this.canWrite || this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		this.saving = true;
		const value = this.form.getRawValue();
		const last = value.slabs.length - 1;
		const payload: EffectiveRatePayload = {
			startDate: value.startDate,
			ratePerUnit: value.ratePerUnit,
			fixedCharge: Number(value.fixedCharge),
			wheelingChargePerUnit: Number(value.wheelingChargePerUnit),
			electricityDutyPercent: Number(value.electricityDutyPercent),
			taxOnSalePaisePerUnit: Number(value.taxOnSalePaisePerUnit),
			// The last slab has no upper limit.
			slabs: value.slabs.map((slab, index) => ({
				upToUnits: index === last ? null : Number(slab.upToUnits),
				ratePerUnit: Number(slab.ratePerUnit),
				adjustmentPerUnit: Number(slab.adjustmentPerUnit)
			}))
		};
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
		if (!this.canDelete || !window.confirm(`Remove the rate starting ${rate.startDate}?`)) return;
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