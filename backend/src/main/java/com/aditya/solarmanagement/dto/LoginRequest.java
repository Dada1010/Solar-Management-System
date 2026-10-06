package com.aditya.solarmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank @Email String emailAddress,
		@NotBlank String password) {
}
