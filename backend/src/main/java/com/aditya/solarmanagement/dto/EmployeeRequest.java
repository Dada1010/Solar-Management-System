package com.aditya.solarmanagement.dto;

import com.aditya.solarmanagement.models.Employee.EmployeeType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.aditya.solarmanagement.validation.ValidMobileNumber;

public record EmployeeRequest(
		@NotNull Long branchId,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@Size(max = 255) String address,
		@ValidMobileNumber @Size(max = 40) String mobileNo,
		@NotBlank @Email @Size(max = 160) String emailAddress,
		@NotNull EmployeeType employeeType) {
}
