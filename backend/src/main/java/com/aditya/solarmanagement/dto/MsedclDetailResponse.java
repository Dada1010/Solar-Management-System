package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import com.aditya.solarmanagement.models.MsedclChargeType;

public record MsedclDetailResponse(
		Long id,
		Long customerId,
		Long companyId,
		Long branchId,
		String branchName,
		String billingUnit,
		String name,
		String mobileNo,
		String consumerNo,
		BigDecimal ratePerUnit,
		boolean electricityDutyApplicable,
		BigDecimal electricityDutyPercent,
		String lastInvoiceNo,
		int dueDays,
		MsedclChargeType chargeType,
		Long referralId,
		String referralName) {
}