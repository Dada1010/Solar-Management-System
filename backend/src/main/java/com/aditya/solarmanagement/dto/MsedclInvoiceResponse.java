package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aditya.solarmanagement.models.MsedclChargeType;

public record MsedclInvoiceResponse(
		Long id,
		Long invoiceId,
		String invoiceNo,
		Long companyId,
		Long branchId,
		Long msedclDetailId,
		String consumerNo,
		String consumerName,
		String billingUnit,
		MsedclChargeType chargeType,
		LocalDate invoiceDate,
		LocalDate billingDate,
		BigDecimal importCurrent,
		BigDecimal importPrevious,
		BigDecimal importConsumption,
		BigDecimal exportCurrent,
		BigDecimal exportPrevious,
		BigDecimal exportConsumption,
		BigDecimal generationCurrent,
		BigDecimal generationPrevious,
		BigDecimal generationConsumption,
		BigDecimal previousBankUnits,
		BigDecimal solarOffsetUnits,
		BigDecimal bankSolarUnits,
		BigDecimal solarBillUnits,
		BigDecimal ratePerUnit,
		String rateSource,
		BigDecimal solarAmount,
		BigDecimal msebBillAmount,
		BigDecimal invoiceAmount,
		BigDecimal paidAmount,
		BigDecimal balanceAmount,
		String msebBillFileName,
		LocalDateTime msebBillUploadedAt) {
}