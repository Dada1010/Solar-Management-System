package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import com.aditya.solarmanagement.models.MsedclChargeType;

public record MsedclDetailResponse(
		Long id,
		Long companyId,
		Long branchId,
		String branchName,
		String billingUnit,
		String name,
		String mobileNo,
		String consumerNo,
		BigDecimal ratePerUnit,
		String lastInvoiceNo,
		MsedclChargeType chargeType) {
}