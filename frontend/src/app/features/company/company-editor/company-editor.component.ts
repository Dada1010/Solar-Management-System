import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { CompanyPayload, CompanyRecord } from '../../../core/models/api.models';

@Component({
	selector: 'app-company-editor',
	templateUrl: './company-editor.component.html',
	styleUrl: './company-editor.component.scss',
	standalone: false
})
export class CompanyEditorComponent implements OnChanges {
	private readonly formBuilder = inject(FormBuilder);
	@Input() visible = false;
	@Input() company: CompanyRecord | null = null;
	@Input() saving = false;
	@Output() visibleChange = new EventEmitter<boolean>();
	@Output() saveCompany = new EventEmitter<CompanyPayload>();
	readonly form = this.formBuilder.nonNullable.group({
		name: ['', [Validators.required, Validators.maxLength(160)]],
		industry: ['', Validators.maxLength(120)],
		address: ['', Validators.maxLength(255)],
		mobileNo: [''],
		emailAddress: ['', Validators.email]
	});

	ngOnChanges(changes: SimpleChanges): void {
		if (this.visible && (changes['visible'] || changes['company'])) this.resetForm();
	}

	handleVisibilityChange(visible: boolean): void {
		this.visibleChange.emit(visible);
	}

	submit(): void {
		if (this.form.invalid || this.saving) {
			this.form.markAllAsTouched();
			return;
		}
		this.saveCompany.emit(this.form.getRawValue());
	}

	private resetForm(): void {
		this.form.reset({
			name: this.company?.name ?? '',
			industry: this.company?.industry ?? '',
			address: this.company?.address ?? '',
			mobileNo: this.company?.mobileNo ?? '',
			emailAddress: this.company?.emailAddress ?? ''
		});
	}
}
