import { Component, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { forkJoin, map, of, Subscription } from 'rxjs';
import {
	BranchRecord,
	EmployeePayload,
	EmployeeRecord,
	EmployeeType,
	MsedclChargeType,
	MsedclDetailPayload,
	MsedclDetailRecord
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
	selector: 'app-people',
	templateUrl: './people.component.html',
	styleUrl: './people.component.scss',
	standalone: false
})
export class PeopleComponent implements OnDestroy {
	private readonly formBuilder = inject(FormBuilder);
	rows: EmployeeRecord[] = [];
	branches: BranchRecord[] = [];
	employeeType: EmployeeType = 'COMPANY_EMPLOYEE';
	title = 'Employees';
	loading = false;
	saving = false;
	dialogVisible = false;
	msedclDialogVisible = false;
	msedclLoading = false;
	msedclSaving = false;
	editingId: number | null = null;
	editingMsedclId: number | null = null;
	selectedCustomer: EmployeeRecord | null = null;
	msedclRows: MsedclDetailRecord[] = [];
	page = 0;
	size = 10;
	totalRecords = 0;
	nameFilter = '';
	private branchesLoaded = false;
	readonly form = this.formBuilder.nonNullable.group({
		firstName: ['', [Validators.required, Validators.maxLength(100)]],
		lastName: ['', [Validators.required, Validators.maxLength(100)]],
		emailAddress: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
		mobileNo: [''],
		address: [''],
		branchId: this.formBuilder.control<number | null>(null, Validators.required)
	});
	readonly msedclForm = this.formBuilder.nonNullable.group({
		billingUnit: ['', Validators.required],
		name: ['', Validators.required],
		mobileNo: ['', Validators.required],
		consumerNo: ['', Validators.required],
		ratePerUnit: [0, [Validators.required, Validators.min(0)]],
		dueDays: [0, [Validators.required, Validators.min(0), Validators.max(365)]],
		chargeType: this.formBuilder.nonNullable.control<MsedclChargeType>('ONLY_SOLAR_GENERATION', Validators.required)
	});
	private readonly routeSubscription: Subscription;

	constructor(
		private readonly route: ActivatedRoute,
		private readonly api: ManagementApiService,
		readonly auth: AuthService,
		private readonly messages: MessageService
	) {
		this.routeSubscription = this.route.data.subscribe((data) => {
			this.employeeType = data['employeeType'] as EmployeeType;
			this.title = this.employeeType === 'CUSTOMER' ? 'Customers' : 'Employees';
			this.page = 0;
			this.nameFilter = '';
			this.loadPage();
		});
	}

	get filterLabel(): string {
		return this.employeeType === 'CUSTOMER' ? 'Filter by customer name' : 'Filter by employee name';
	}

	get canWrite(): boolean {
		return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
	}

	get canDelete(): boolean {
		return this.auth.user?.role === 'ADMIN';
	}

	ngOnDestroy(): void {
		this.routeSubscription.unsubscribe();
	}

	loadPage(): void {
		this.loading = true;
		const branchRequest = this.branchesLoaded
			? of(this.branches)
			: this.auth.user?.companyId
				? this.api.branchesForCompany(this.auth.user.companyId, 0, 100).pipe(map((result) => result.items))
				: this.api.branches(0, 100).pipe(map((result) => result.items));
		forkJoin({
			page: this.api.employeesByType(this.employeeType, this.page, this.size, this.nameFilter),
			branches: branchRequest
		}).subscribe({
			next: (result) => {
				this.rows = result.page.items;
				this.totalRecords = result.page.totalElements;
				this.branches = result.branches as BranchRecord[];
				this.branchesLoaded = true;
				this.loading = false;
			},
			error: (error: unknown) => this.handleError('Could not load people', error)
		});
	}

	applyFilter(): void {
		this.page = 0;
		this.loadPage();
	}

	clearFilter(): void {
		this.nameFilter = '';
		this.applyFilter();
	}

	changePage(event: { page?: number | null; rows?: number | null }): void {
		this.page = event.page ?? 0;
		this.size = event.rows ?? this.size;
		this.loadPage();
	}

	create(): void {
		if (!this.canWrite) return;
		this.editingId = null;
		this.form.reset({ firstName: '', lastName: '', emailAddress: '', mobileNo: '', address: '', branchId: this.branches[0]?.id ?? null });
		this.dialogVisible = true;
	}

	edit(person: EmployeeRecord): void {
		if (!this.canWrite) return;
		this.loading = true;
		this.api.employee(person.id).subscribe({
			next: (record) => {
				this.editingId = record.id;
				this.form.patchValue({
					firstName: record.firstName,
					lastName: record.lastName,
					emailAddress: record.emailAddress,
					mobileNo: record.mobileNo ?? '',
					address: record.address ?? '',
					branchId: record.branchId
				});
				this.loading = false;
				this.dialogVisible = true;
			},
			error: (error: unknown) => this.handleError('Could not load person', error)
		});
	}

	openMsedclDetails(customer: EmployeeRecord): void {
		this.selectedCustomer = customer;
		this.editingMsedclId = null;
		this.msedclForm.reset({ billingUnit: '', name: '', mobileNo: '', consumerNo: '', ratePerUnit: 0, dueDays: 0, chargeType: 'ONLY_SOLAR_GENERATION' });
		this.msedclDialogVisible = true;
		this.loadMsedclDetails();
	}

	editMsedclDetail(detail: MsedclDetailRecord): void {
		if (!this.canWrite) return;
		this.editingMsedclId = detail.id;
		this.msedclForm.patchValue({
			billingUnit: detail.billingUnit,
			name: detail.name,
			mobileNo: detail.mobileNo,
			consumerNo: detail.consumerNo,
			ratePerUnit: detail.ratePerUnit,
			dueDays: detail.dueDays,
			chargeType: detail.chargeType
		});
	}

	resetMsedclForm(): void {
		this.editingMsedclId = null;
		this.msedclForm.reset({ billingUnit: '', name: '', mobileNo: '', consumerNo: '', ratePerUnit: 0, dueDays: 0, chargeType: 'ONLY_SOLAR_GENERATION' });
	}

	saveMsedclDetail(): void {
		if (!this.canWrite || !this.selectedCustomer || this.msedclForm.invalid) {
			this.msedclForm.markAllAsTouched();
			return;
		}
		this.msedclSaving = true;
		const payload: MsedclDetailPayload = this.msedclForm.getRawValue();
		const request = this.editingMsedclId === null
			? this.api.addCustomerMsedclDetail(this.selectedCustomer.id, payload)
			: this.api.updateCustomerMsedclDetail(this.selectedCustomer.id, this.editingMsedclId, payload);
		request.subscribe({
			next: () => {
				this.msedclSaving = false;
				this.resetMsedclForm();
				this.messages.add({ severity: 'success', summary: 'Saved', detail: 'MSEDCL detail saved successfully.' });
				this.loadMsedclDetails();
			},
			error: (error: unknown) => {
				this.msedclSaving = false;
				this.handleError('Could not save MSEDCL detail', error);
			}
		});
	}

	removeMsedclDetail(detail: MsedclDetailRecord): void {
		if (!this.canDelete || !this.selectedCustomer || !window.confirm(`Remove consumer ${detail.consumerNo}?`)) return;
		this.api.deleteCustomerMsedclDetail(this.selectedCustomer.id, detail.id).subscribe({
			next: () => {
				if (this.editingMsedclId === detail.id) this.resetMsedclForm();
				this.messages.add({ severity: 'success', summary: 'Removed', detail: 'MSEDCL detail removed.' });
				this.loadMsedclDetails();
			},
			error: (error: unknown) => this.handleError('Could not remove MSEDCL detail', error)
		});
	}

	removePerson(person: EmployeeRecord): void {
		const label = this.employeeType === 'CUSTOMER' ? 'customer' : 'employee';
		if (!this.canDelete || !window.confirm(`Delete ${label} ${person.firstName} ${person.lastName}?`)) return;
		this.api.deleteEmployee(person.id).subscribe({
			next: () => {
				this.messages.add({ severity: 'success', summary: 'Deleted', detail: `${label} deleted.` });
				this.loadPage();
			},
			error: (error: unknown) => this.handleError('Could not delete person', error)
		});
	}

	resetPassword(person: EmployeeRecord): void {
		const label = this.employeeType === 'CUSTOMER' ? 'customer' : 'employee';
		if (!this.canDelete || !window.confirm(`Reset the password for ${person.firstName} ${person.lastName}? They must change it after signing in.`)) return;
		this.api.resetEmployeePassword(person.id).subscribe({
			next: (result) => this.messages.add({ severity: 'success', summary: 'Password reset',
				detail: result.message, life: 15000 }),
			error: (error: unknown) => this.handleError(`Could not reset ${label} password`, error)
		});
	}

	private loadMsedclDetails(): void {
		if (!this.selectedCustomer) return;
		this.msedclLoading = true;
		this.api.customerMsedclDetails(this.selectedCustomer.id).subscribe({
			next: (details) => {
				this.msedclRows = details;
				this.msedclLoading = false;
			},
			error: (error: unknown) => {
				this.msedclLoading = false;
				this.handleError('Could not load MSEDCL details', error);
			}
		});
	}

	save(): void {
		if (!this.canWrite || this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		this.saving = true;
		const value = this.form.getRawValue();
		const payload: EmployeePayload = { ...value, branchId: Number(value.branchId), employeeType: this.employeeType };
		const request = this.editingId === null
			? this.api.createEmployee(payload)
			: this.api.updateEmployee(this.editingId, payload);
		request.subscribe({
			next: () => {
				this.saving = false;
				this.dialogVisible = false;
				this.page = 0;
				this.messages.add({ severity: 'success', summary: 'Saved', detail: `${this.title.slice(0, -1)} saved successfully.` });
				this.loadPage();
			},
			error: (error: unknown) => {
				this.saving = false;
				this.handleError('Could not save person', error);
			}
		});
	}

	private handleError(summary: string, error: unknown): void {
		this.loading = false;
		const responseMessage = typeof error === 'object' && error !== null && 'error' in error
			? (error as { error?: { message?: string } }).error?.message
			: undefined;
		this.messages.add({ severity: 'error', summary, detail: responseMessage ?? 'Please try again.' });
	}
}