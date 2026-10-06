package com.aditya.solarmanagement.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.DashboardResponse;
import com.aditya.solarmanagement.dto.DueInvoiceReportRow;
import com.aditya.solarmanagement.dto.MsedclInvoiceResponse;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EmployeeRole;
import com.aditya.solarmanagement.service.InvoiceService;
import com.aditya.solarmanagement.repo.EmployeeRepository;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
	private final EmployeeRepository employees;
	private final InvoiceService invoices;

	public DashboardController(EmployeeRepository employees, InvoiceService invoices) {
		this.employees = employees;
		this.invoices = invoices;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(Authentication authentication) {
		Employee requester = employees.findByEmailAddressIgnoreCase(authentication.getName())
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "Account not found"));
		long customerCount = switch (requester.getRole()) {
			case ADMIN -> employees.countByEmployeeType(Employee.EmployeeType.CUSTOMER);
			case USER -> employees.countByEmployeeTypeAndBranch_Id(Employee.EmployeeType.CUSTOMER,
					requester.getBranch().getId());
			case CUSTOMER -> requester.getEmployeeType() == Employee.EmployeeType.CUSTOMER ? 1 : 0;
		};
		List<MsedclInvoiceResponse> invoiceRecords = invoices.invoices(authentication.getName(), null, null, null,
				null, null, null, null);
		LocalDate today = LocalDate.now();
		List<DueInvoiceReportRow> dueInvoices = invoiceRecords.stream()
				.filter(invoice -> invoice.status() == com.aditya.solarmanagement.models.InvoiceStatus.OPEN
						&& invoice.balanceAmount().compareTo(BigDecimal.ZERO) > 0)
				.map(invoice -> new DueInvoiceReportRow(invoice.id(), invoice.invoiceNo(), invoice.originalInvoiceNo(),
						invoice.consumerName(), invoice.consumerNo(), invoice.dueDate(), invoice.invoiceAmount(),
						invoice.paidAmount(), invoice.balanceAmount(), invoice.dueDate().isBefore(today)))
				.sorted(Comparator.comparing(DueInvoiceReportRow::dueDate).thenComparing(DueInvoiceReportRow::invoiceNo))
				.toList();
		long overdueCount = dueInvoices.stream().filter(DueInvoiceReportRow::overdue).count();
		BigDecimal outstanding = dueInvoices.stream().map(DueInvoiceReportRow::balanceAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		DashboardResponse response = new DashboardResponse(customerCount, dueInvoices.size(), overdueCount,
				outstanding, dueInvoices);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Dashboard retrieved successfully", response));
	}
}
