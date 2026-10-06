package com.aditya.solarmanagement.dto;

public record BranchResponse(
		Long id,
		Long companyId,
		String companyName,
		String name,
		String address,
		String mobileNo,
		String emailAddress) {
}
