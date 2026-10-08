package com.aditya.solarmanagement.dto;

import java.math.BigDecimal;

public record InvoiceOtherChargeResponse(Long reasonId, String reasonName, BigDecimal amount) {
}
