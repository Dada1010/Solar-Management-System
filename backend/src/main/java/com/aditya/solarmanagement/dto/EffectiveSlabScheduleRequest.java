package com.aditya.solarmanagement.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EffectiveSlabScheduleRequest(@NotNull LocalDate startDate,
		@Valid List<EffectiveRateSlabRequest> slabs) {
	public EffectiveSlabScheduleRequest {
		slabs = slabs == null ? List.of() : List.copyOf(slabs);
	}
}