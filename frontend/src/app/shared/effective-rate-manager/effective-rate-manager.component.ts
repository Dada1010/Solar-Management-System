import { Component, Input, inject } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MenuItem, MessageService } from 'primeng/api';
import { Menu } from 'primeng/menu';
import {
	EffectiveRateOwnerType,
	EffectiveRatePayload,
	EffectiveRateRecord,
	EffectiveSlabSchedulePayload,
	EffectiveSlabScheduleRecord
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
	slabSchedules: EffectiveSlabScheduleRecord[] = [];
	dialogVisible = false;
	loadingRates = false;
	loadingSlabs = false;
	saving = false;
	editingRateId: number | null = null;
	editingSlabScheduleId: number | null = null;
	rateActionItems: MenuItem[] = [];
	slabActionItems: MenuItem[] = [];
	private activeRate: EffectiveRateRecord | null = null;
	private activeSchedule: EffectiveSlabScheduleRecord | null = null;
	readonly rateForm = this.formBuilder.nonNullable.group({
		startDate: ['', Validators.required],
		ratePerUnit: [0, [Validators.required, Validators.min(0)]],
		fixedCharge: [0, [Validators.required, Validators.min(0)]],
		wheelingChargePerUnit: [0, [Validators.required, Validators.min(0)]],
		electricityDutyPercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
		taxOnSalePaisePerUnit: [0, [Validators.required, Validators.min(0)]]
	});
	readonly slabForm = this.formBuilder.nonNullable.group({
		startDate: ['', Validators.required],
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
		this.resetRateForm();
		this.resetSlabForm();
		this.loadRates();
		this.loadSlabSchedules();
	}

	editRate(rate: EffectiveRateRecord): void {
		if (!this.canWrite) return;
		this.editingRateId = rate.id;
		this.rateForm.patchValue({
			startDate: rate.startDate,
			ratePerUnit: rate.ratePerUnit,
			fixedCharge: rate.fixedCharge,
			wheelingChargePerUnit: rate.wheelingChargePerUnit,
			electricityDutyPercent: rate.electricityDutyPercent,
			taxOnSalePaisePerUnit: rate.taxOnSalePaisePerUnit
		});
	}

	editSlabSchedule(schedule: EffectiveSlabScheduleRecord): void {
		if (!this.canWrite) return;
		this.editingSlabScheduleId = schedule.id;
		this.slabForm.patchValue({ startDate: schedule.startDate });
		this.slabRows.clear();
		schedule.slabs.forEach((slab) => this.slabRows.push(this.slabGroup(slab.upToUnits, slab.ratePerUnit, slab.adjustmentPerUnit)));
	}

	get slabRows(): FormArray<FormGroup<SlabControls>> {
		return this.slabForm.controls.slabs;
	}

	addSlab(): void {
		this.slabRows.push(this.slabGroup(null, 0, 0));
	}

	removeSlab(index: number): void {
		this.slabRows.removeAt(index);
	}

	slabSummary(schedule: EffectiveSlabScheduleRecord): string {
		let from = 0;
		return schedule.slabs.map((slab) => {
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

	saveRate(): void {
		if (!this.canWrite || this.rateForm.invalid) {
			this.rateForm.markAllAsTouched();
			return;
		}
		this.saving = true;
		const value = this.rateForm.getRawValue();
		const payload: EffectiveRatePayload = {
			startDate: value.startDate,
			ratePerUnit: value.ratePerUnit,
			fixedCharge: Number(value.fixedCharge),
			wheelingChargePerUnit: Number(value.wheelingChargePerUnit),
			electricityDutyPercent: Number(value.electricityDutyPercent),
			taxOnSalePaisePerUnit: Number(value.taxOnSalePaisePerUnit)
		};
		const request = this.editingRateId === null
			? this.api.addEffectiveRate(this.ownerType, this.ownerId, payload)
			: this.api.updateEffectiveRate(this.ownerType, this.ownerId, this.editingRateId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.resetRateForm();
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Effective rate saved.' });
				this.loadRates();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not save effective rate', error);
			}
		});
	}

	saveSlabSchedule(): void {
		if (!this.canWrite || this.slabForm.invalid || !this.validSlabBands()) {
			this.slabForm.markAllAsTouched();
			if (!this.validSlabBands()) this.messages.add({ severity: 'error', summary: 'Invalid slabs', detail: 'Enter increasing limits for every slab except the final unlimited slab.' });
			return;
		}
		this.saving = true;
		const value = this.slabForm.getRawValue();
		const last = value.slabs.length - 1;
		const payload: EffectiveSlabSchedulePayload = {
			startDate: value.startDate,
			slabs: value.slabs.map((slab, index) => ({
				upToUnits: index === last ? null : Number(slab.upToUnits),
				ratePerUnit: Number(slab.ratePerUnit),
				adjustmentPerUnit: Number(slab.adjustmentPerUnit)
			}))
		};
		const request = this.editingSlabScheduleId === null
			? this.api.addEffectiveSlabSchedule(this.ownerType, this.ownerId, payload)
			: this.api.updateEffectiveSlabSchedule(this.ownerType, this.ownerId, this.editingSlabScheduleId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.resetSlabForm();
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'Slab schedule saved.' });
				this.loadSlabSchedules();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.showError('Could not save slab schedule', error);
			}
		});
	}

	openRateActions(rate: EffectiveRateRecord, event: Event, menu: Menu): void {
		this.activeRate = rate;
		this.rateActionItems = this.actionItems(
			() => this.activeRate && this.editRate(this.activeRate),
			() => this.activeRate && this.removeRate(this.activeRate)
		);
		menu.toggle(event);
	}

	openSlabActions(schedule: EffectiveSlabScheduleRecord, event: Event, menu: Menu): void {
		this.activeSchedule = schedule;
		this.slabActionItems = this.actionItems(
			() => this.activeSchedule && this.editSlabSchedule(this.activeSchedule),
			() => this.activeSchedule && this.removeSlabSchedule(this.activeSchedule)
		);
		menu.toggle(event);
	}

	private actionItems(edit: () => void, remove: () => void): MenuItem[] {
		return [
			{ label: 'Edit', icon: 'pi pi-pencil', visible: this.canWrite, command: edit },
			{ label: 'Delete', icon: 'pi pi-trash', visible: this.canDelete, command: remove }
		];
	}

	private removeRate(rate: EffectiveRateRecord): void {
		if (!this.canDelete || !window.confirm(`Remove the rate starting ${rate.startDate}?`)) return;
		this.api.deleteEffectiveRate(this.ownerType, this.ownerId, rate.id).subscribe({
			next: () => {
				if (this.editingRateId === rate.id) this.resetRateForm();
				this.messages.add({ severity: 'success', summary: 'Removed', detail: 'Effective rate removed.' });
				this.loadRates();
			},
			error: (error: unknown) => this.showError('Could not remove effective rate', error)
		});
	}

	private removeSlabSchedule(schedule: EffectiveSlabScheduleRecord): void {
		if (!this.canDelete || !window.confirm(`Remove the slab schedule starting ${schedule.startDate}?`)) return;
		this.api.deleteEffectiveSlabSchedule(this.ownerType, this.ownerId, schedule.id).subscribe({
			next: () => {
				if (this.editingSlabScheduleId === schedule.id) this.resetSlabForm();
				this.messages.add({ severity: 'success', summary: 'Removed', detail: 'Slab schedule removed.' });
				this.loadSlabSchedules();
			},
			error: (error: unknown) => this.showError('Could not remove slab schedule', error)
		});
	}

	resetRateForm(): void {
		this.editingRateId = null;
		this.rateForm.reset({ startDate: '', ratePerUnit: 0, fixedCharge: 0, wheelingChargePerUnit: 0,
			electricityDutyPercent: 0,
			taxOnSalePaisePerUnit: 0 });
	}

	resetSlabForm(): void {
		this.editingSlabScheduleId = null;
		this.slabRows.clear();
		this.slabForm.reset({ startDate: '' });
		this.addSlab();
	}

	private validSlabBands(): boolean {
		if (this.slabRows.length === 0) return false;
		let previous = 0;
		for (let index = 0; index < this.slabRows.length - 1; index++) {
			const limit = this.slabRows.at(index).controls.upToUnits.value;
			if (limit === null || limit <= previous) return false;
			previous = limit;
		}
		return this.slabRows.controls.every((row) => row.controls.ratePerUnit.valid && row.controls.adjustmentPerUnit.valid);
	}

	private loadRates(): void {
		this.loadingRates = true;
		this.api.effectiveRates(this.ownerType, this.ownerId).subscribe({
			next: (rates) => {
				this.rates = rates;
				this.loadingRates = false;
			},
			error: (error: unknown) => {
				this.loadingRates = false;
				this.showError('Could not load effective rates', error);
			}
		});
	}

	private loadSlabSchedules(): void {
		this.loadingSlabs = true;
		this.api.effectiveSlabSchedules(this.ownerType, this.ownerId).subscribe({
			next: (schedules) => {
				this.slabSchedules = schedules;
				this.loadingSlabs = false;
			},
			error: (error: unknown) => {
				this.loadingSlabs = false;
				this.showError('Could not load slab schedules', error);
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