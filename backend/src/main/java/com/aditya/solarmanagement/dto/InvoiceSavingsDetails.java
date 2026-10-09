package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

/** Saved calculation behind the consumer savings figure; amounts are as billed on the invoice date. */
public record InvoiceSavingsDetails(
		BigDecimal directSolarUnits,
		BigDecimal gridImportUnits,
		BigDecimal totalUnits,
		BigDecimal netGridUnits,
		TariffBillDetails withoutSolar,
		TariffBillDetails withSolarGrid,
		BigDecimal withSolarGridAmount,
		boolean gridAmountEntered,
		BigDecimal solarBillUnits,
		BigDecimal solarRatePerUnit,
		BigDecimal solarAmount,
		BigDecimal electricityDutyPercent,
		BigDecimal electricityDutyAmount) {

	public record TariffBand(BigDecimal fromUnits, BigDecimal upToUnits, BigDecimal units, BigDecimal ratePerUnit,
			BigDecimal adjustmentPerUnit, BigDecimal amount, BigDecimal adjustmentAmount) {
	}

	public record TariffBillDetails(BigDecimal units, List<TariffBand> bands, BigDecimal fixedCharge,
			BigDecimal wheelingChargePerUnit, BigDecimal wheelingAmount, BigDecimal fuelAdjustmentAmount,
			BigDecimal electricityDutyPercent,
			BigDecimal electricityDutyAmount, BigDecimal taxOnSalePaisePerUnit, BigDecimal taxOnSaleAmount,
			BigDecimal totalAmount) {
	}
}
