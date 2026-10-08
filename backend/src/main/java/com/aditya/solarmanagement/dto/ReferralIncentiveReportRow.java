package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

public record ReferralIncentiveReportRow(Long referralId, String referralName, String month,
		long invoiceCount, BigDecimal solarAmount, BigDecimal incentiveAmount) {
}