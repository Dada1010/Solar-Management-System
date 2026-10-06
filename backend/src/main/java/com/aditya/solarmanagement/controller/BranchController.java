package com.aditya.solarmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.BranchRequest;
import com.aditya.solarmanagement.dto.BranchResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.service.ManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class BranchController {
	private final ManagementService managementService;

	public BranchController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping("/branches")
	public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> branches(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String name) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Branches retrieved successfully",
				managementService.branches(page, size, name)));
	}

	@GetMapping("/branches/{id}")
	public ResponseEntity<ApiResponse<BranchResponse>> branch(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Branch retrieved successfully",
				managementService.branch(id)));
	}

	@GetMapping("/companies/{companyId}/branches")
	public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> branchesForCompany(@PathVariable Long companyId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String name) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Branches retrieved successfully",
				managementService.branchesForCompany(companyId, page, size, name)));
	}

	@PostMapping("/branches")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<BranchResponse>> addBranch(@Valid @RequestBody BranchRequest request) {
		BranchResponse data = managementService.addBranch(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED, "Branch created successfully", data));
	}

	@PutMapping("/branches/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(@PathVariable Long id,
			@Valid @RequestBody BranchRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Branch updated successfully",
				managementService.updateBranch(id, request)));
	}
}