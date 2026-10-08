package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

public record InvoiceReferralResponse(Long id, String name, BigDecimal percentage, BigDecimal incentiveAmount) {
}
