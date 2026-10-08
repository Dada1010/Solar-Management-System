package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EffectiveRateResponse(Long id, LocalDate startDate, BigDecimal ratePerUnit,
		List<EffectiveRateSlabResponse> slabs, BigDecimal fixedCharge, BigDecimal wheelingChargePerUnit,
		BigDecimal electricityDutyPercent, BigDecimal taxOnSalePaisePerUnit) {
}