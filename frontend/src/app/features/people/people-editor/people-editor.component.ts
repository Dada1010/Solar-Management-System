import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BranchRecord, EmployeePayload, EmployeeRecord, EmployeeType } from '../../../core/models/api.models';

@Component({
	selector: 'app-people-editor',
	templateUrl: './people-editor.component.html',
	styleUrl: './people-editor.component.scss',
	standalone: false
})
export class PeopleEditorComponent implements OnChanges {
	private readonly formBuilder = inject(FormBuilder);
	@Input() visible = false;
	@Input() employeeType: EmployeeType = 'COMPANY_EMPLOYEE';
	@Input() person: EmployeeRecord | null = null;
	@Input() branches: BranchRecord[] = [];
	@Input() saving = false;
	@Output() visibleChange = new EventEmitter<boolean>();
	@Output() savePerson = new EventEmitter<EmployeePayload>();
	readonly form = this.formBuilder.nonNullable.group({
		firstName: ['', [Validators.required, Validators.maxLength(100)]],
		lastName: ['', [Validators.required, Validators.maxLength(100)]],
		emailAddress: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
		mobileNo: [''],
		address: [''],
		branchId: this.formBuilder.control<number | null>(null, Validators.required),
		referralPercentage: this.formBuilder.control<number | null>(null)
	});

	get isReferral(): boolean {
		return this.employeeType === 'REFERRAL';
	}

	get accountLabel(): string {
		return this.employeeType === 'CUSTOMER' ? 'customer' : this.isReferral ? 'referral' : 'employee';
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (this.visible && (changes['visible'] || changes['person'] || changes['branches'])) this.resetForm();
	}

	handleVisibilityChange(visible: boolean): void {
		this.visibleChange.emit(visible);
	}

	submit(): void {
		if (this.form.invalid || this.saving) {
			this.form.markAllAsTouched();
			return;
		}
		const value = this.form.getRawValue();
		this.savePerson.emit({
			...value,
			branchId: Number(value.branchId),
			employeeType: this.employeeType,
			referralPercentage: this.isReferral ? Number(value.referralPercentage) : null
		});
	}

	private resetForm(): void {
		const percentage = this.form.controls.referralPercentage;
		percentage.setValidators(this.isReferral ? [Validators.required, Validators.min(0), Validators.max(100)] : []);
		this.form.reset({
			firstName: this.person?.firstName ?? '',
			lastName: this.person?.lastName ?? '',
			emailAddress: this.person?.emailAddress ?? '',
			mobileNo: this.person?.mobileNo ?? '',
			address: this.person?.address ?? '',
			branchId: this.person?.branchId ?? this.branches[0]?.id ?? null,
			referralPercentage: this.person?.referralPercentage ?? null
		});
	}
}
