import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BranchPayload, BranchRecord, CompanyRecord } from '../../../core/models/api.models';
import { AuthService } from '../../../core/services/auth.service';

@Component({
	selector: 'app-branch-editor',
	templateUrl: './branch-editor.component.html',
	styleUrl: './branch-editor.component.scss',
	standalone: false
})
export class BranchEditorComponent implements OnChanges {
	private readonly formBuilder = inject(FormBuilder);
	@Input() visible = false;
	@Input() branch: BranchRecord | null = null;
	@Input() companies: CompanyRecord[] = [];
	@Input() saving = false;
	@Output() visibleChange = new EventEmitter<boolean>();
	@Output() saveBranch = new EventEmitter<BranchPayload>();
	readonly form = this.formBuilder.group({
		name: this.formBuilder.nonNullable.control('', [Validators.required, Validators.maxLength(160)]),
		address: this.formBuilder.nonNullable.control(''),
		mobileNo: this.formBuilder.nonNullable.control(''),
		emailAddress: this.formBuilder.nonNullable.control('', Validators.email),
		companyId: this.formBuilder.control<number | null>(null, Validators.required)
	});

	constructor(readonly auth: AuthService) {}

	get selectedCompanyName(): string {
		const id = this.form.controls.companyId.value;
		return this.companies.find((company) => company.id === id)?.name ?? this.auth.user?.companyName ?? '';
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (this.visible && (changes['visible'] || changes['branch'] || changes['companies'])) this.resetForm();
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
		this.saveBranch.emit({ ...value, companyId: Number(value.companyId) });
	}

	private resetForm(): void {
		this.form.reset({
			name: this.branch?.name ?? '',
			address: this.branch?.address ?? '',
			mobileNo: this.branch?.mobileNo ?? '',
			emailAddress: this.branch?.emailAddress ?? '',
			companyId: this.branch?.companyId ?? this.auth.user?.companyId ?? this.companies[0]?.id ?? null
		});
	}
}
