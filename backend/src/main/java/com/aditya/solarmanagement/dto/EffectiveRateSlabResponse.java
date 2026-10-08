package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

public record EffectiveRateSlabResponse(BigDecimal upToUnits, BigDecimal ratePerUnit, BigDecimal adjustmentPerUnit) {
}
