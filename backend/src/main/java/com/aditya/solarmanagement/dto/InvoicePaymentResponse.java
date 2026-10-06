package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoicePaymentResponse(Long id, Long invoiceId, LocalDate paymentDate, BigDecimal amount, String note) {
}