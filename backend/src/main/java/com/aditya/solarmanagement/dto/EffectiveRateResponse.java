package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EffectiveRateResponse(Long id, LocalDate startDate, BigDecimal ratePerUnit) {
}