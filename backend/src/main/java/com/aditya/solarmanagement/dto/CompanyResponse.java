package com.aditya.solarmanagement.dto;

public record CompanyResponse(
		Long id,
		String name,
		String industry,
		String address,
		String mobileNo,
		String emailAddress,
		long branchCount) {
}
