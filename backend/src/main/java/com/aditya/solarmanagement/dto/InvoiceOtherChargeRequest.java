package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record InvoiceOtherChargeRequest(
		@NotNull Long reasonId,
		@DecimalMin("0.0") @Digits(integer = 12, fraction = 2) BigDecimal amount) {
	public InvoiceOtherChargeRequest {
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
	}
}
