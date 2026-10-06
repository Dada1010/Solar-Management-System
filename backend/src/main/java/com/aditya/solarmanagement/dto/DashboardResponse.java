package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(long totalCustomers, long dueInvoiceCount, long overdueInvoiceCount,
		BigDecimal outstandingAmount, List<DueInvoiceReportRow> dueInvoices) {
}
