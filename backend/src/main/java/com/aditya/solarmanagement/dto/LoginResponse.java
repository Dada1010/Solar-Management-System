package com.aditya.solarmanagement.dto;

public record LoginResponse(
		String token,
		String tokenType,
		long expiresInSeconds,
		String emailAddress,
		String name,
		String role,
		boolean mustChangePassword,
		Long companyId,
		String companyName) {
}
