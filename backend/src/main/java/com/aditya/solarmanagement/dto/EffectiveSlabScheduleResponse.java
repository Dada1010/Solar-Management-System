package com.aditya.solarmanagement.dto;

import java.time.LocalDate;
import java.util.List;

public record EffectiveSlabScheduleResponse(Long id, LocalDate startDate,
		List<EffectiveRateSlabResponse> slabs) {
}