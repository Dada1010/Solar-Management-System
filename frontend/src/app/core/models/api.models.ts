export type InvoicePaymentType = 'CASH' | 'UPI';
export interface ApiResponse<T> {
  status: boolean;
  code: number;
  message: string;
  data: T;
}

export type EmployeeType = 'CUSTOMER' | 'COMPANY_EMPLOYEE';
export type EmployeeRole = 'ADMIN' | 'USER' | 'CUSTOMER';
export type EffectiveRateOwnerType = 'COMPANY' | 'BRANCH' | 'MSEDCL_DETAIL';

export interface EffectiveRateRecord {
  id: number;
  startDate: string;
  ratePerUnit: number;
}

export interface EffectiveRatePayload {
  startDate: string;
  ratePerUnit: number;
}

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
  companyId: number;
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

export interface MsedclDetailRecord {
  id: number;
  customerId: number;
  companyId: number;
  branchId: number;
  branchName: string;
  billingUnit: string;
  name: string;
  mobileNo: string;
  consumerNo: string;
  ratePerUnit: number;
  lastInvoiceNo: string | null;
  dueDays: number;
  chargeType: MsedclChargeType;
}

export type MsedclChargeType = 'ONLY_SOLAR_GENERATION' | 'SOLAR_PLUS_MSEB_BILL_AMOUNT';

export interface MsedclDetailPayload {
  billingUnit: string;
  name: string;
  mobileNo: string;
  consumerNo: string;
  ratePerUnit: number;
  chargeType: MsedclChargeType;
  dueDays: number;
}

export interface MsedclInvoicePayload {
  msedclDetailId: number;
  invoiceDate: string;
  billingDate: string;
  importCurrent: number;
  importPrevious: number;
  exportCurrent: number;
  exportPrevious: number;
  generationCurrent: number;
  generationPrevious: number;
  previousBankUnits: number;
  msebBillAmount: number | null;
}

export interface InvoiceFilters {
  invoiceNo?: string;
  consumerName?: string;
  consumerNo?: string;
  paymentStatus?: 'PAID' | 'UNPAID';
  invoiceStatus?: 'OPEN' | 'CANCELLED';
  invoiceDateFrom?: string;
  invoiceDateTo?: string;
}

export interface MsedclInvoiceRecord {
  id: number | null;
  invoiceId: number | null;
  invoiceNo: string | null;
  companyId: number;
  branchId: number;
  msedclDetailId: number;
  consumerNo: string;
  consumerName: string;
  billingUnit: string;
  chargeType: MsedclChargeType;
  status: 'OPEN' | 'CANCELLED';
  reversalOfInvoiceId: number | null;
  originalInvoiceNo: string | null;
  invoiceDate: string;
  billingDate: string;
  dueDays: number;
  dueDate: string;
  importCurrent: number;
  importPrevious: number;
  importConsumption: number;
  exportCurrent: number;
  exportPrevious: number;
  exportConsumption: number;
  generationCurrent: number;
  generationPrevious: number;
  generationConsumption: number;
  previousBankUnits: number;
  solarOffsetUnits: number;
  bankSolarUnits: number;
  solarBillUnits: number;
  ratePerUnit: number;
  rateSource: string;
  solarAmount: number;
  msebBillAmount: number;
  invoiceAmount: number;
  paidAmount: number;
  balanceAmount: number;
  msebBillFileName: string | null;
  msebBillUploadedAt: string | null;
}

export interface InvoicePaymentRecord {
  id: number;
  invoiceId: number;
  invoiceNo: string;
  invoiceStatus: 'OPEN' | 'CANCELLED';
  consumerName: string;
  consumerNo: string;
  paymentDate: string;
  amount: number;
  paymentType: InvoicePaymentType;
  transactionNo: string | null;
  note: string | null;
  entryType: 'PAYMENT' | 'REVERSAL';
  reversalOfPaymentId: number | null;
  reversed: boolean;
}

export interface InvoicePaymentPayload {
  paymentDate: string;
  amount: number;
  paymentType: InvoicePaymentType;
  transactionNo: string | null;
  note: string;
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

export interface DashboardReportRow {
  invoiceId: number;
  invoiceNo: string;
  originalInvoiceNo: string | null;
  consumerName: string;
  consumerNo: string;
  dueDate: string;
  invoiceAmount: number;
  paidAmount: number;
  balanceAmount: number;
  overdue: boolean;
}

export interface DashboardRecord {
  totalCustomers: number;
  dueInvoiceCount: number;
  overdueInvoiceCount: number;
  outstandingAmount: number;
  dueInvoices: DashboardReportRow[];
}