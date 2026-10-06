package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aditya.solarmanagement.models.InvoicePaymentType;

public record InvoicePaymentResponse(Long id, Long invoiceId, String invoiceNo, String consumerName,
		String consumerNo, LocalDate paymentDate, BigDecimal amount, InvoicePaymentType paymentType,
		String transactionNo, String note) {
}