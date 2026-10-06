package com.aditya.solarmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.InvoicePaymentResponse;
import com.aditya.solarmanagement.service.InvoiceService;

@RestController
@RequestMapping("/api/v1/payment-details")
public class PaymentDetailsController {
	private final InvoiceService invoiceService;

	public PaymentDetailsController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<InvoicePaymentResponse>>> payments(Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment details retrieved successfully",
				invoiceService.allPayments(authentication.getName())));
	}
}