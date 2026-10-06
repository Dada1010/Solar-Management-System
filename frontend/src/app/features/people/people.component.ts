import { Component, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { forkJoin, map, of, Subscription } from 'rxjs';
import { BranchRecord, EmployeePayload, EmployeeRecord, EmployeeType } from '../../core/models/api.models';
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
	editingId: number | null = null;
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
		this.editingId = null;
		this.form.reset({ firstName: '', lastName: '', emailAddress: '', mobileNo: '', address: '', branchId: this.branches[0]?.id ?? null });
		this.dialogVisible = true;
	}

	edit(person: EmployeeRecord): void {
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

	save(): void {
		if (this.form.invalid) {
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