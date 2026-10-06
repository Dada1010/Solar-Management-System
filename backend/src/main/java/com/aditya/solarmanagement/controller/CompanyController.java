package com.aditya.solarmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.CompanyRequest;
import com.aditya.solarmanagement.dto.CompanyResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.service.ManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
	private final ManagementService managementService;

	public CompanyController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> companies(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Companies retrieved successfully",
				managementService.companies(page, size)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CompanyResponse>> company(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Company retrieved successfully",
				managementService.company(id)));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<CompanyResponse>> addCompany(@Valid @RequestBody CompanyRequest request) {
		CompanyResponse data = managementService.addCompany(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED, "Company created successfully", data));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(@PathVariable Long id,
			@Valid @RequestBody CompanyRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Company updated successfully",
				managementService.updateCompany(id, request)));
	}
}