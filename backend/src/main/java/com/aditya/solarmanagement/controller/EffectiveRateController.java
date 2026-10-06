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
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.EffectiveRateRequest;
import com.aditya.solarmanagement.dto.EffectiveRateResponse;
import com.aditya.solarmanagement.models.EffectiveRateOwnerType;
import com.aditya.solarmanagement.service.ManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/effective-rates")
public class EffectiveRateController {
	private final ManagementService managementService;

	public EffectiveRateController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping("/{ownerType}/{ownerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'CUSTOMER')")
	public ResponseEntity<ApiResponse<List<EffectiveRateResponse>>> rates(@PathVariable EffectiveRateOwnerType ownerType,
			@PathVariable Long ownerId, Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Effective rates retrieved successfully",
				managementService.effectiveRates(ownerType, ownerId, authentication.getName())));
	}

	@PostMapping("/{ownerType}/{ownerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<EffectiveRateResponse>> add(@PathVariable EffectiveRateOwnerType ownerType,
			@PathVariable Long ownerId, @Valid @RequestBody EffectiveRateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED,
				"Effective rate added successfully", managementService.addEffectiveRate(ownerType, ownerId, request)));
	}

	@PutMapping("/{ownerType}/{ownerId}/{rateId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<EffectiveRateResponse>> update(@PathVariable EffectiveRateOwnerType ownerType,
			@PathVariable Long ownerId, @PathVariable Long rateId, @Valid @RequestBody EffectiveRateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Effective rate updated successfully",
				managementService.updateEffectiveRate(ownerType, ownerId, rateId, request)));
	}

	@DeleteMapping("/{ownerType}/{ownerId}/{rateId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable EffectiveRateOwnerType ownerType,
			@PathVariable Long ownerId, @PathVariable Long rateId) {
		managementService.deleteEffectiveRate(ownerType, ownerId, rateId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Effective rate deleted successfully", null));
	}
}