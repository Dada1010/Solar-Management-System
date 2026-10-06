package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aditya.solarmanagement.models.InvoicePaymentType;
import com.aditya.solarmanagement.models.InvoicePaymentEntryType;
import com.aditya.solarmanagement.models.InvoiceStatus;

public record InvoicePaymentResponse(Long id, Long invoiceId, String invoiceNo, InvoiceStatus invoiceStatus, String consumerName,
		String consumerNo, LocalDate paymentDate, BigDecimal amount, InvoicePaymentType paymentType,
		String transactionNo, String note, InvoicePaymentEntryType entryType, Long reversalOfPaymentId,
		boolean reversed) {
}