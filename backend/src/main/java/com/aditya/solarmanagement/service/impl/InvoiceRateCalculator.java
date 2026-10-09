package com.aditya.solarmanagement.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.aditya.solarmanagement.models.EffectiveSlabSchedule.SlabSpec;

final class InvoiceRateCalculator {
	private InvoiceRateCalculator() {}

	static BigDecimal weightedSlabRate(List<SlabSpec> slabs, BigDecimal units) {
		if (slabs.isEmpty() || units == null || units.signum() <= 0) return null;
		BigDecimal amount = BigDecimal.ZERO;
		BigDecimal remaining = units;
		BigDecimal from = BigDecimal.ZERO;
		for (SlabSpec slab : slabs) {
			if (remaining.signum() <= 0) break;
			BigDecimal band = slab.upToUnits() == null ? remaining
					: remaining.min(slab.upToUnits().subtract(from));
			amount = amount.add(band.multiply(slab.ratePerUnit().add(slab.adjustmentPerUnit())));
			remaining = remaining.subtract(band);
			if (slab.upToUnits() != null) from = slab.upToUnits();
		}
		BigDecimal coveredUnits = units.subtract(remaining);
		return coveredUnits.signum() == 0 ? null : amount.divide(coveredUnits, 4, RoundingMode.HALF_UP);
	}

	static boolean useSlabAverage(BigDecimal configuredRate, BigDecimal slabAverageRate) {
		return slabAverageRate != null && slabAverageRate.compareTo(configuredRate) < 0;
	}

	static BigDecimal selectRate(BigDecimal configuredRate, BigDecimal slabAverageRate) {
		return useSlabAverage(configuredRate, slabAverageRate) ? slabAverageRate : configuredRate;
	}

	static BigDecimal electricityDuty(BigDecimal solarEnergyAmount, BigDecimal dutyPercent) {
		return solarEnergyAmount.multiply(dutyPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}
}