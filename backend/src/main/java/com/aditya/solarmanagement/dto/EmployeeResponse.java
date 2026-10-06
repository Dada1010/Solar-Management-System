package com.aditya.solarmanagement.dto;

import com.aditya.solarmanagement.models.Employee.EmployeeType;
import com.aditya.solarmanagement.models.EmployeeRole;

public record EmployeeResponse(
		Long id,
		Long branchId,
		String branchName,
		String firstName,
		String lastName,
		String address,
		String mobileNo,
		String emailAddress,
		EmployeeType employeeType,
		EmployeeRole role,
		boolean mustChangePassword) {
}
