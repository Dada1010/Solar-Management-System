package com.aditya.solarmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.MsedclDetailResponse;
import com.aditya.solarmanagement.dto.MsedclDetailRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import com.aditya.solarmanagement.service.ManagementService;

@RestController
@RequestMapping("/api/v1/consumer-details")
public class ConsumerDetailsController {
	private final ManagementService managementService;

	public ConsumerDetailsController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<MsedclDetailResponse>>> consumerDetails(Authentication authentication,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String mobileNo,
			@RequestParam(required = false) String consumerNo) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Consumer details retrieved successfully",
				managementService.consumerDetails(authentication.getName(), name, mobileNo, consumerNo)));
	}

	@PutMapping("/{detailId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclDetailResponse>> updateConsumerDetail(Authentication authentication,
			@PathVariable Long detailId, @Valid @RequestBody MsedclDetailRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Consumer detail updated successfully",
				managementService.updateConsumerDetail(authentication.getName(), detailId, request)));
	}
}