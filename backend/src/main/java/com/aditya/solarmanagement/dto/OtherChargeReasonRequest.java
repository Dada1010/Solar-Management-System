package com.aditya.solarmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtherChargeReasonRequest(@NotBlank @Size(max = 100) String name) {
}
