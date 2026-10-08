package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record EffectiveRateSlabRequest(
		@DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 10, fraction = 4) BigDecimal upToUnits,
		@NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal ratePerUnit,
		@DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal adjustmentPerUnit) {
	public EffectiveRateSlabRequest {
		if (adjustmentPerUnit == null) {
			adjustmentPerUnit = BigDecimal.ZERO;
		}
	}
}
