package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

import com.aditya.solarmanagement.validation.ValidMobileNumber;
import com.aditya.solarmanagement.models.MsedclChargeType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record MsedclDetailRequest(
		@NotBlank @Size(max = 50) String billingUnit,
		@NotBlank @Size(max = 100) String name,
		@NotBlank @ValidMobileNumber @Size(max = 40) String mobileNo,
		@NotBlank @Size(max = 50) String consumerNo,
		@NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal ratePerUnit,
		@NotNull MsedclChargeType chargeType,
		@NotNull @Min(0) @Max(365) Integer dueDays,
		Long referralId) {
	public MsedclDetailRequest {
		if (chargeType == null) {
			chargeType = MsedclChargeType.ONLY_SOLAR_GENERATION;
		}
	}
}