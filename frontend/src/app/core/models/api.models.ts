export interface ApiResponse<T> {
  status: boolean;
  code: number;
  message: string;
  data: T;
}

export type EmployeeType = 'CUSTOMER' | 'COMPANY_EMPLOYEE';
export type EmployeeRole = 'ADMIN' | 'USER' | 'CUSTOMER';

export interface LoginResult {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  emailAddress: string;
  name: string;
  role: EmployeeRole;
  mustChangePassword: boolean;
  companyId: number;
  companyName: string;
}

export interface MessageResult {
  message: string;
}

export interface CompanyRecord {
  id: number;
  name: string;
  industry: string | null;
  address: string | null;
  mobileNo: string | null;
  emailAddress: string | null;
  branchCount: number;
}

export interface BranchRecord {
  id: number;
  companyId: number;
  companyName: string;
  name: string;
  address: string | null;
  mobileNo: string | null;
  emailAddress: string | null;
}

export interface EmployeeRecord {
  id: number;
  branchId: number;
  branchName: string;
  firstName: string;
  lastName: string;
  address: string | null;
  mobileNo: string | null;
  emailAddress: string;
  employeeType: EmployeeType;
  role: EmployeeRole;
  mustChangePassword: boolean;
}

export interface CompanyPayload {
  name: string;
  industry: string;
  address: string;
  mobileNo: string;
  emailAddress: string;
}

export interface BranchPayload {
  companyId: number;
  name: string;
  address: string;
  mobileNo: string;
  emailAddress: string;
}

export interface EmployeePayload {
  branchId: number;
  firstName: string;
  lastName: string;
  address: string;
  mobileNo: string;
  emailAddress: string;
  employeeType: EmployeeType;
}

export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}