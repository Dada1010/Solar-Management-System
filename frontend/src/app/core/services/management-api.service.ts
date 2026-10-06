import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  BranchPayload,
  BranchRecord,
  CompanyPayload,
  CompanyRecord,
  DashboardRecord,
  EmployeePayload,
  EmployeeRecord,
  EmployeeType,
  MessageResult,
  EffectiveRateOwnerType,
  EffectiveRatePayload,
  EffectiveRateRecord,
  InvoicePaymentPayload,
  InvoicePaymentRecord,
  InvoiceFilters,
  MsedclDetailPayload,
  MsedclDetailRecord,
  MsedclInvoicePayload,
  MsedclInvoiceRecord,
  PageResult
} from '../models/api.models';
import { unwrap } from './api-envelope';

@Injectable({ providedIn: 'root' })
export class ManagementApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  companies(page = 0, size = 20): Observable<PageResult<CompanyRecord>> {
    return this.getPage('/companies', page, size);
  }

  dashboard(): Observable<DashboardRecord> {
    return this.get('/dashboard');
  }

  company(id: number): Observable<CompanyRecord> {
    return this.get(`/companies/${id}`);
  }

  createCompany(payload: CompanyPayload): Observable<CompanyRecord> {
    return this.post('/companies', payload);
  }

  updateCompany(id: number, payload: CompanyPayload): Observable<CompanyRecord> {
    return this.put(`/companies/${id}`, payload);
  }

  deleteCompany(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/companies/${id}`).pipe(map(unwrap));
  }

  branches(page = 0, size = 20, name?: string): Observable<PageResult<BranchRecord>> {
    return this.getPage('/branches', page, size, name);
  }

  branch(id: number): Observable<BranchRecord> {
    return this.get(`/branches/${id}`);
  }

  branchesForCompany(companyId: number, page = 0, size = 20, name?: string): Observable<PageResult<BranchRecord>> {
    return this.getPage(`/companies/${companyId}/branches`, page, size, name);
  }

  createBranch(payload: BranchPayload): Observable<BranchRecord> {
    return this.post('/branches', payload);
  }

  updateBranch(id: number, payload: BranchPayload): Observable<BranchRecord> {
    return this.put(`/branches/${id}`, payload);
  }

  deleteBranch(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/branches/${id}`).pipe(map(unwrap));
  }

  employees(page = 0, size = 20, name?: string): Observable<PageResult<EmployeeRecord>> {
    return this.getPage('/employees', page, size, name);
  }

  employeesByType(employeeType: EmployeeType, page = 0, size = 20, name?: string): Observable<PageResult<EmployeeRecord>> {
    return this.getPage(`/employees/by-type/${employeeType}`, page, size, name);
  }

  currentEmployee(): Observable<EmployeeRecord> {
    return this.get('/employees/me');
  }

  employee(id: number): Observable<EmployeeRecord> {
    return this.get(`/employees/${id}`);
  }

  createEmployee(payload: EmployeePayload): Observable<EmployeeRecord> {
    return this.post('/employees', payload);
  }

  updateEmployee(id: number, payload: EmployeePayload): Observable<EmployeeRecord> {
    return this.put(`/employees/${id}`, payload);
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/employees/${id}`).pipe(map(unwrap));
  }

  resetEmployeePassword(id: number): Observable<MessageResult> {
    return this.post(`/employees/${id}/reset-password`, {});
  }

  customerMsedclDetails(customerId: number): Observable<MsedclDetailRecord[]> {
    return this.get(`/employees/${customerId}/msedcl-details`);
  }

  consumerDetails(name?: string, mobileNo?: string, consumerNo?: string): Observable<MsedclDetailRecord[]> {
    let params = new HttpParams();
    if (name?.trim()) params = params.set('name', name.trim());
    if (mobileNo?.trim()) params = params.set('mobileNo', mobileNo.trim());
    if (consumerNo?.trim()) params = params.set('consumerNo', consumerNo.trim());
    return this.http.get<ApiResponse<MsedclDetailRecord[]>>(`${this.baseUrl}/consumer-details`, { params }).pipe(map(unwrap));
  }

  updateConsumerMsedclDetail(detailId: number, payload: MsedclDetailPayload): Observable<MsedclDetailRecord> {
    return this.put(`/consumer-details/${detailId}`, payload);
  }

  invoices(filters?: InvoiceFilters): Observable<MsedclInvoiceRecord[]> {
    let params = new HttpParams();
    if (filters?.invoiceNo?.trim()) params = params.set('invoiceNo', filters.invoiceNo.trim());
    if (filters?.consumerName?.trim()) params = params.set('consumerName', filters.consumerName.trim());
    if (filters?.consumerNo?.trim()) params = params.set('consumerNo', filters.consumerNo.trim());
    if (filters?.paymentStatus) params = params.set('paymentStatus', filters.paymentStatus);
    if (filters?.invoiceStatus) params = params.set('invoiceStatus', filters.invoiceStatus);
    if (filters?.invoiceDateFrom) params = params.set('invoiceDateFrom', filters.invoiceDateFrom);
    if (filters?.invoiceDateTo) params = params.set('invoiceDateTo', filters.invoiceDateTo);
    return this.http.get<ApiResponse<MsedclInvoiceRecord[]>>(`${this.baseUrl}/invoices`, { params }).pipe(map(unwrap));
  }

  paymentDetails(): Observable<InvoicePaymentRecord[]> {
    return this.get('/payment-details');
  }

  previewInvoice(payload: MsedclInvoicePayload): Observable<MsedclInvoiceRecord> {
    return this.post('/invoices/preview', payload);
  }

  createInvoice(payload: MsedclInvoicePayload): Observable<MsedclInvoiceRecord> {
    return this.post('/invoices', payload);
  }

  cancelInvoice(invoiceId: number): Observable<void> {
    return this.post(`/invoices/${invoiceId}/cancel`, {});
  }

  invoicePayments(invoiceId: number): Observable<InvoicePaymentRecord[]> {
    return this.get(`/invoices/${invoiceId}/payments`);
  }

  addInvoicePayment(invoiceId: number, payload: InvoicePaymentPayload): Observable<InvoicePaymentRecord[]> {
    return this.post(`/invoices/${invoiceId}/payments`, payload);
  }

  cancelInvoicePayment(invoiceId: number, paymentId: number): Observable<void> {
    return this.post(`/invoices/${invoiceId}/payments/${paymentId}/cancel`, {});
  }

  uploadInvoiceMsebBill(invoiceId: number, file: File): Observable<MsedclInvoiceRecord> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<ApiResponse<MsedclInvoiceRecord>>(`${this.baseUrl}/invoices/${invoiceId}/mseb-bill`, body)
      .pipe(map(unwrap));
  }

  downloadInvoiceMsebBill(invoiceId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/invoices/${invoiceId}/mseb-bill`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  addCustomerMsedclDetail(customerId: number, payload: MsedclDetailPayload): Observable<MsedclDetailRecord> {
    return this.post(`/employees/${customerId}/msedcl-details`, payload);
  }

  updateCustomerMsedclDetail(customerId: number, detailId: number, payload: MsedclDetailPayload): Observable<MsedclDetailRecord> {
    return this.put(`/employees/${customerId}/msedcl-details/${detailId}`, payload);
  }

  deleteCustomerMsedclDetail(customerId: number, detailId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/employees/${customerId}/msedcl-details/${detailId}`).pipe(map(unwrap));
  }

  effectiveRates(ownerType: EffectiveRateOwnerType, ownerId: number): Observable<EffectiveRateRecord[]> {
    return this.get(`/effective-rates/${ownerType}/${ownerId}`);
  }

  addEffectiveRate(ownerType: EffectiveRateOwnerType, ownerId: number, payload: EffectiveRatePayload): Observable<EffectiveRateRecord> {
    return this.post(`/effective-rates/${ownerType}/${ownerId}`, payload);
  }

  updateEffectiveRate(ownerType: EffectiveRateOwnerType, ownerId: number, rateId: number, payload: EffectiveRatePayload): Observable<EffectiveRateRecord> {
    return this.put(`/effective-rates/${ownerType}/${ownerId}/${rateId}`, payload);
  }

  deleteEffectiveRate(ownerType: EffectiveRateOwnerType, ownerId: number, rateId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/effective-rates/${ownerType}/${ownerId}/${rateId}`).pipe(map(unwrap));
  }

  private get<T>(path: string): Observable<T> {
    return this.http.get<ApiResponse<T>>(`${this.baseUrl}${path}`).pipe(map(unwrap));
  }

  private getPage<T>(path: string, page: number, size: number, name?: string): Observable<PageResult<T>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (name?.trim()) params = params.set('name', name.trim());
    return this.http.get<ApiResponse<PageResult<T>>>(`${this.baseUrl}${path}`, { params }).pipe(map(unwrap));
  }

  private post<T>(path: string, payload: unknown): Observable<T> {
    return this.http.post<ApiResponse<T>>(`${this.baseUrl}${path}`, payload).pipe(map(unwrap));
  }

  private put<T>(path: string, payload: unknown): Observable<T> {
    return this.http.put<ApiResponse<T>>(`${this.baseUrl}${path}`, payload).pipe(map(unwrap));
  }
}