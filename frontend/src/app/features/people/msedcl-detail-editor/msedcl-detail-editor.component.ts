import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { EmployeeRecord, MsedclChargeType, MsedclDetailPayload, MsedclDetailRecord } from '../../../core/models/api.models';
import { ManagementApiService } from '../../../core/services/management-api.service';

@Component({
	selector: 'app-msedcl-detail-editor',
	templateUrl: './msedcl-detail-editor.component.html',
	styleUrl: './msedcl-detail-editor.component.scss',
	standalone: false
})
export class MsedclDetailEditorComponent implements OnInit, OnChanges {
	private readonly formBuilder = inject(FormBuilder);
	private readonly api = inject(ManagementApiService);
	referrals: EmployeeRecord[] = [];
	@Input() detail: MsedclDetailRecord | null = null;
	@Input() canWrite = false;
	@Input() saving = false;
	@Output() saveDetail = new EventEmitter<MsedclDetailPayload>();
	@Output() cancelEdit = new EventEmitter<void>();
	readonly form = this.formBuilder.nonNullable.group({
		billingUnit: ['', Validators.required],
		name: ['', Validators.required],
		mobileNo: ['', Validators.required],
		consumerNo: ['', Validators.required],
		ratePerUnit: [0, [Validators.required, Validators.min(0)]],
		electricityDutyApplicable: [false],
		electricityDutyPercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
		dueDays: [0, [Validators.required, Validators.min(0), Validators.max(365)]],
		chargeType: this.formBuilder.nonNullable.control<MsedclChargeType>('ONLY_SOLAR_GENERATION', Validators.required),
		referralId: this.formBuilder.control<number | null>(null)
	});

	ngOnInit(): void {
		if (!this.canWrite) return;
		// Referral list is optional; the editor still works if it cannot be loaded.
		this.api.employeesByType('REFERRAL', 0, 100).subscribe({
			next: (result) => this.referrals = result.items,
			error: () => this.referrals = []
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['detail']) this.resetForm();
	}

	submit(): void {
		if (!this.canWrite || this.form.invalid || this.saving) {
			this.form.markAllAsTouched();
			return;
		}
		const value = this.form.getRawValue();
		this.saveDetail.emit({ ...value,
			electricityDutyPercent: value.electricityDutyApplicable ? value.electricityDutyPercent : 0 });
	}

	cancel(): void {
		this.cancelEdit.emit();
	}

	private resetForm(): void {
		this.form.reset({
			billingUnit: this.detail?.billingUnit ?? '',
			name: this.detail?.name ?? '',
			mobileNo: this.detail?.mobileNo ?? '',
			consumerNo: this.detail?.consumerNo ?? '',
			ratePerUnit: this.detail?.ratePerUnit ?? 0,
			electricityDutyApplicable: this.detail?.electricityDutyApplicable ?? false,
			electricityDutyPercent: this.detail?.electricityDutyPercent ?? 0,
			dueDays: this.detail?.dueDays ?? 0,
			chargeType: this.detail?.chargeType ?? 'ONLY_SOLAR_GENERATION',
			referralId: this.detail?.referralId ?? null
		});
	}
}
