package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import com.aditya.solarmanagement.models.MsedclChargeType;

public record MsedclDetailResponse(
		Long id,
		String billingUnit,
		String name,
		String mobileNo,
		String consumerNo,
		BigDecimal ratePerUnit,
		MsedclChargeType chargeType) {
}