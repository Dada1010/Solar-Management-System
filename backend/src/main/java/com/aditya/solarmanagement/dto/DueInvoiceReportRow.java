package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DueInvoiceReportRow(Long invoiceId, String invoiceNo, String originalInvoiceNo, String consumerName,
		String consumerNo, LocalDate dueDate, BigDecimal invoiceAmount, BigDecimal paidAmount,
		BigDecimal balanceAmount, boolean overdue) {
}
