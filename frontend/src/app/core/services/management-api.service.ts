import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  BranchPayload,
  BranchRecord,
  CompanyPayload,
  CompanyRecord,
  EmployeePayload,
  EmployeeRecord,
  EmployeeType,
  MsedclDetailPayload,
  MsedclDetailRecord,
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

  company(id: number): Observable<CompanyRecord> {
    return this.get(`/companies/${id}`);
  }

  createCompany(payload: CompanyPayload): Observable<CompanyRecord> {
    return this.post('/companies', payload);
  }

  updateCompany(id: number, payload: CompanyPayload): Observable<CompanyRecord> {
    return this.put(`/companies/${id}`, payload);
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

  employees(page = 0, size = 20, name?: string): Observable<PageResult<EmployeeRecord>> {
    return this.getPage('/employees', page, size, name);
  }

  employeesByType(employeeType: EmployeeType, page = 0, size = 20, name?: string): Observable<PageResult<EmployeeRecord>> {
    return this.getPage(`/employees/by-type/${employeeType}`, page, size, name);
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

  customerMsedclDetails(customerId: number): Observable<MsedclDetailRecord[]> {
    return this.get(`/employees/${customerId}/msedcl-details`);
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