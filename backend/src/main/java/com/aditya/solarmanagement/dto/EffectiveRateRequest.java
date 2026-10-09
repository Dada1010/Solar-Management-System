package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record EffectiveRateRequest(
		@NotNull LocalDate startDate,
		@NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal ratePerUnit,
		@DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal fixedCharge,
		@DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal wheelingChargePerUnit,
		@DecimalMin("0.0") @Digits(integer = 6, fraction = 2) BigDecimal taxOnSalePaisePerUnit) {
	public EffectiveRateRequest {
		fixedCharge = fixedCharge == null ? BigDecimal.ZERO : fixedCharge;
		wheelingChargePerUnit = wheelingChargePerUnit == null ? BigDecimal.ZERO : wheelingChargePerUnit;
		taxOnSalePaisePerUnit = taxOnSalePaisePerUnit == null ? BigDecimal.ZERO : taxOnSalePaisePerUnit;
	}
}