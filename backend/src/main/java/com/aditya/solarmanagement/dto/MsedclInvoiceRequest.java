package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record MsedclInvoiceRequest(
		@NotNull Long msedclDetailId,
		@NotNull LocalDate invoiceDate,
		@NotNull LocalDate billingDate,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal importCurrent,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal importPrevious,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal exportCurrent,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal exportPrevious,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal generationCurrent,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal generationPrevious,
		@NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 4) BigDecimal previousBankUnits,
		@DecimalMin("0.0") @Digits(integer = 12, fraction = 2) BigDecimal msebBillAmount) {
}