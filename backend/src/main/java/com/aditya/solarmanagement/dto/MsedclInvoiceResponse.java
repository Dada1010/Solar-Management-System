package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.aditya.solarmanagement.models.MsedclChargeType;
import com.aditya.solarmanagement.models.InvoiceStatus;

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
		InvoiceStatus status,
		Long reversalOfInvoiceId,
		String originalInvoiceNo,
		LocalDate invoiceDate,
		LocalDate billingDate,
		int dueDays,
		LocalDate dueDate,
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
		BigDecimal otherChargesAmount,
		List<InvoiceOtherChargeResponse> otherCharges,
		BigDecimal invoiceAmount,
		BigDecimal paidAmount,
		BigDecimal balanceAmount,
		String msebBillFileName,
		LocalDateTime msebBillUploadedAt,
		InvoiceReferralResponse referral,
		BigDecimal totalConsumptionUnits,
		BigDecimal withoutSolarBillAmount,
		BigDecimal withSolarBillAmount,
		BigDecimal consumerSavingsAmount) {
}