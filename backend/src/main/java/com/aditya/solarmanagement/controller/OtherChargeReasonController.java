package com.aditya.solarmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.OtherChargeReasonRequest;
import com.aditya.solarmanagement.dto.OtherChargeReasonResponse;
import com.aditya.solarmanagement.service.OtherChargeReasonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/other-charge-reasons")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class OtherChargeReasonController {
	private final OtherChargeReasonService reasons;

	public OtherChargeReasonController(OtherChargeReasonService reasons) {
		this.reasons = reasons;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<OtherChargeReasonResponse>>> list() {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reasons retrieved successfully", reasons.list()));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<OtherChargeReasonResponse>> add(@Valid @RequestBody OtherChargeReasonRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
				ApiResponse.success(HttpStatus.CREATED, "Reason created successfully", reasons.add(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<OtherChargeReasonResponse>> rename(@PathVariable Long id,
			@Valid @RequestBody OtherChargeReasonRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reason updated successfully",
				reasons.rename(id, request)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		reasons.delete(id);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Reason deleted successfully", null));
	}
}
