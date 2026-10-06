package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.aditya.solarmanagement.models.InvoicePaymentType;

public record InvoicePaymentRequest(
		@NotNull LocalDate paymentDate,
		@NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
		@NotNull InvoicePaymentType paymentType,
		@Size(max = 100) String transactionNo,
		@Size(max = 255) String note) {
}