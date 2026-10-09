import { Component, OnDestroy, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { filter, Subscription } from 'rxjs';
import { MsedclDetailRecord, MsedclInvoicePayload, MsedclInvoiceRecord } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ManagementApiService } from '../../core/services/management-api.service';

@Component({
  selector: 'app-workspace-shell',
  templateUrl: './workspace-shell.component.html',
  styleUrl: './workspace-shell.component.scss',
  standalone: false
})
export class WorkspaceShellComponent implements OnDestroy {
  private readonly formBuilder = inject(FormBuilder);
  activeTitle = 'Dashboard';
  navOpen = false;
  calculatorVisible = false;
  calculatorLoadingDetails = false;
  calculatorRunning = false;
  calculatorError = '';
  calculatorDetails: MsedclDetailRecord[] = [];
  calculatorResult: MsedclInvoiceRecord | null = null;
  private calculatorDetailsLoaded = false;
  readonly calculatorForm = this.formBuilder.group({
    msedclDetailId: this.formBuilder.control<number | null>(null, Validators.required),
    importCurrent: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    importPrevious: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    exportCurrent: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    exportPrevious: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    generationCurrent: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    generationPrevious: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    previousBankUnits: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    msebBillAmount: this.formBuilder.control<number | null>(null, Validators.min(0))
  });
  readonly profileMenu: MenuItem[];
  private readonly routeSubscription: Subscription;

  constructor(private readonly router: Router, readonly auth: AuthService,
		private readonly api: ManagementApiService) {
    this.profileMenu = [
      { label: 'Signed in', disabled: true },
      { separator: true },
      ...(this.canUseSavingsCalculator
        ? [{ label: 'Savings calculator', icon: 'pi pi-calculator', command: () => this.openSavingsCalculator() }, { separator: true }]
        : []),
      { label: 'Sign out', icon: 'pi pi-sign-out', command: () => this.signOut() }
    ];
    this.setTitle(router.url);
    this.routeSubscription = router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event) => {
      this.setTitle(event.urlAfterRedirects);
      this.navOpen = false;
    });
  }

  get userInitials(): string {
    return (this.auth.user?.name ?? 'User').split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase();
  }

  get canUseSavingsCalculator(): boolean {
    return this.auth.user?.role === 'ADMIN' || this.auth.user?.role === 'USER';
  }

  get calculatorDetail(): MsedclDetailRecord | undefined {
    const id = this.calculatorForm.controls.msedclDetailId.value;
    return this.calculatorDetails.find((detail) => detail.id === id);
  }

  openSavingsCalculator(): void {
    this.calculatorVisible = true;
    this.calculatorResult = null;
    this.calculatorError = '';
    if (this.calculatorDetailsLoaded || this.calculatorLoadingDetails) return;
    this.calculatorLoadingDetails = true;
    this.api.consumerDetails().subscribe({
      next: (details) => {
        this.calculatorDetails = details;
        this.calculatorDetailsLoaded = true;
        this.calculatorForm.controls.msedclDetailId.setValue(details[0]?.id ?? null);
        this.updateBillAmountValidation();
        this.calculatorLoadingDetails = false;
      },
      error: () => {
        this.calculatorError = 'Could not load consumer accounts.';
        this.calculatorLoadingDetails = false;
      }
    });
  }

  calculateSavings(): void {
    if (this.calculatorForm.invalid) {
      this.calculatorForm.markAllAsTouched();
      return;
    }
    const detail = this.calculatorDetail;
    if (!detail) return;
    const values = this.calculatorForm.getRawValue();
    const readings = [values.importCurrent, values.importPrevious, values.exportCurrent,
      values.exportPrevious, values.generationCurrent, values.generationPrevious, values.previousBankUnits];
    if (readings.some((value) => value === null)) return;
    const today = this.today();
    const payload: MsedclInvoicePayload = {
      msedclDetailId: detail.id,
      invoiceDate: today,
      billingDate: today,
      importCurrent: values.importCurrent ?? 0,
      importPrevious: values.importPrevious ?? 0,
      exportCurrent: values.exportCurrent ?? 0,
      exportPrevious: values.exportPrevious ?? 0,
      generationCurrent: values.generationCurrent ?? 0,
      generationPrevious: values.generationPrevious ?? 0,
      previousBankUnits: values.previousBankUnits ?? 0,
      msebBillAmount: detail.chargeType === 'SOLAR_PLUS_MSEB_BILL_AMOUNT' ? values.msebBillAmount : null,
      otherCharges: []
    };
    this.calculatorRunning = true;
    this.calculatorError = '';
    this.api.previewInvoice(payload).subscribe({
      next: (result) => {
        this.calculatorResult = result;
        this.calculatorRunning = false;
      },
      error: () => {
        this.calculatorError = 'Could not calculate savings. Check the readings and tariff setup.';
        this.calculatorRunning = false;
      }
    });
  }

  onCalculatorDetailChange(): void {
    this.calculatorResult = null;
    this.updateBillAmountValidation();
  }

  signOut(): void {
    this.auth.clearSession();
    void this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.routeSubscription.unsubscribe();
  }

  private today(): string {
    const now = new Date();
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  }

  private updateBillAmountValidation(): void {
    const control = this.calculatorForm.controls.msebBillAmount;
    if (this.calculatorDetail?.chargeType === 'SOLAR_PLUS_MSEB_BILL_AMOUNT') {
      control.addValidators(Validators.required);
    } else {
      control.removeValidators(Validators.required);
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  private setTitle(url: string): void {
    const path = url.split('?')[0].replace(/^\//, '').split('/')[0];
    const titles: Record<string, string> = {
      dashboard: 'Dashboard',
      companies: 'Companies',
      branches: 'Branches',
      employees: 'Employees',
      customers: 'Customers',
      referrals: 'Referrals',
      'referral-report': 'Referral Report',
      'other-charges': 'Reason / Other Charges',
      'consumer-details': 'Consumer Details',
      invoices: 'Invoices',
      'payment-details': 'Payment Details'
    };
    this.activeTitle = path === 'customers' && this.auth.user?.role === 'CUSTOMER'
      ? 'Customer Details'
      : titles[path] ?? 'Dashboard';
  }
}