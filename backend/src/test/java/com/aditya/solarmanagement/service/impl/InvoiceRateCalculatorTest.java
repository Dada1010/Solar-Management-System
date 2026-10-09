package com.aditya.solarmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aditya.solarmanagement.models.EffectiveSlabSchedule.SlabSpec;

class InvoiceRateCalculatorTest {
	@Test
	void calculatesConsumptionWeightedSlabAverageIncludingAdjustments() {
		List<SlabSpec> slabs = List.of(
				new SlabSpec(new BigDecimal("100"), new BigDecimal("3.96"), new BigDecimal("0.15")),
				new SlabSpec(null, new BigDecimal("10.80"), new BigDecimal("0.30")));

		BigDecimal average = InvoiceRateCalculator.weightedSlabRate(slabs, new BigDecimal("200"));

		assertEquals(0, new BigDecimal("7.6050").compareTo(average));
	}

	@Test
	void selectsOnlyTheLowerAvailableRate() {
		assertTrue(InvoiceRateCalculator.useSlabAverage(new BigDecimal("8.00"), new BigDecimal("7.6050")));
		assertEquals(0, new BigDecimal("7.6050").compareTo(
				InvoiceRateCalculator.selectRate(new BigDecimal("8.00"), new BigDecimal("7.6050"))));
		assertFalse(InvoiceRateCalculator.useSlabAverage(new BigDecimal("7.00"), new BigDecimal("7.6050")));
		assertEquals(0, new BigDecimal("7.00").compareTo(
				InvoiceRateCalculator.selectRate(new BigDecimal("7.00"), new BigDecimal("7.6050"))));
	}

	@Test
	void roundsDutyFromSolarEnergyChargeToPaise() {
		assertEquals(0, new BigDecimal("20.09").compareTo(
				InvoiceRateCalculator.electricityDuty(new BigDecimal("125.55"), new BigDecimal("16"))));
	}

	@Test
	void returnsNoAverageWhenNoConsumptionCanBePriced() {
		assertNull(InvoiceRateCalculator.weightedSlabRate(List.of(), new BigDecimal("20")));
		assertNull(InvoiceRateCalculator.weightedSlabRate(
				List.of(new SlabSpec(null, BigDecimal.ONE, BigDecimal.ZERO)), BigDecimal.ZERO));
	}
}