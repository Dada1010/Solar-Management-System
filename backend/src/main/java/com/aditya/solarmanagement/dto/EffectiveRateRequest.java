package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record EffectiveRateRequest(
		@NotNull LocalDate startDate,
		@NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal ratePerUnit,
		@Valid List<EffectiveRateSlabRequest> slabs,
		@DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal fixedCharge,
		@DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal wheelingChargePerUnit,
		@DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal electricityDutyPercent,
		@DecimalMin("0.0") @Digits(integer = 6, fraction = 2) BigDecimal taxOnSalePaisePerUnit) {
	public EffectiveRateRequest {
		slabs = slabs == null ? List.of() : List.copyOf(slabs);
		fixedCharge = fixedCharge == null ? BigDecimal.ZERO : fixedCharge;
		wheelingChargePerUnit = wheelingChargePerUnit == null ? BigDecimal.ZERO : wheelingChargePerUnit;
		electricityDutyPercent = electricityDutyPercent == null ? BigDecimal.ZERO : electricityDutyPercent;
		taxOnSalePaisePerUnit = taxOnSalePaisePerUnit == null ? BigDecimal.ZERO : taxOnSalePaisePerUnit;
	}
}