import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MsedclChargeType, MsedclDetailPayload, MsedclDetailRecord } from '../../../core/models/api.models';

@Component({
	selector: 'app-msedcl-detail-editor',
	templateUrl: './msedcl-detail-editor.component.html',
	styleUrl: './msedcl-detail-editor.component.scss',
	standalone: false
})
export class MsedclDetailEditorComponent implements OnChanges {
	private readonly formBuilder = inject(FormBuilder);
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
		dueDays: [0, [Validators.required, Validators.min(0), Validators.max(365)]],
		chargeType: this.formBuilder.nonNullable.control<MsedclChargeType>('ONLY_SOLAR_GENERATION', Validators.required)
	});

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['detail']) this.resetForm();
	}

	submit(): void {
		if (!this.canWrite || this.form.invalid || this.saving) {
			this.form.markAllAsTouched();
			return;
		}
		this.saveDetail.emit(this.form.getRawValue());
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
			dueDays: this.detail?.dueDays ?? 0,
			chargeType: this.detail?.chargeType ?? 'ONLY_SOLAR_GENERATION'
		});
	}
}
