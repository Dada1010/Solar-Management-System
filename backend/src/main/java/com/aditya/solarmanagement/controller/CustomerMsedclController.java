package com.aditya.solarmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.MsedclDetailRequest;
import com.aditya.solarmanagement.dto.MsedclDetailResponse;
import com.aditya.solarmanagement.service.ManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/employees/{customerId}/msedcl-details")
public class CustomerMsedclController {
	private final ManagementService managementService;

	public CustomerMsedclController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<MsedclDetailResponse>>> details(@PathVariable Long customerId,
			Authentication authentication,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String mobileNo,
			@RequestParam(required = false) String consumerNo) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "MSEDCL details retrieved successfully",
				managementService.msedclDetails(customerId, authentication.getName(), name, mobileNo, consumerNo)));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclDetailResponse>> add(@PathVariable Long customerId,
			Authentication authentication, @Valid @RequestBody MsedclDetailRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED,
				"MSEDCL detail added successfully",
				managementService.addMsedclDetail(authentication.getName(), customerId, request)));
	}

	@PutMapping("/{detailId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclDetailResponse>> update(@PathVariable Long customerId,
			@PathVariable Long detailId, Authentication authentication,
			@Valid @RequestBody MsedclDetailRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "MSEDCL detail updated successfully",
				managementService.updateMsedclDetail(authentication.getName(), customerId, detailId, request)));
	}

	@DeleteMapping("/{detailId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long customerId, @PathVariable Long detailId,
			Authentication authentication) {
		managementService.deleteMsedclDetail(authentication.getName(), customerId, detailId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "MSEDCL detail deleted successfully", null));
	}
}