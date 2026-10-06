package com.aditya.solarmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.aditya.solarmanagement.validation.ValidMobileNumber;

public record BranchRequest(
		@NotNull Long companyId,
		@NotBlank @Size(max = 160) String name,
		@Size(max = 255) String address,
		@ValidMobileNumber @Size(max = 40) String mobileNo,
		@Email @Size(max = 160) String emailAddress) {
}
