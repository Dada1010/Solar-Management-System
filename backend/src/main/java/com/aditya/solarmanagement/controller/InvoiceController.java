package com.aditya.solarmanagement.controller;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.InvoicePaymentRequest;
import com.aditya.solarmanagement.dto.InvoicePaymentResponse;
import com.aditya.solarmanagement.dto.MsedclInvoiceRequest;
import com.aditya.solarmanagement.dto.MsedclInvoiceResponse;
import com.aditya.solarmanagement.models.InvoicePaymentStatus;
import com.aditya.solarmanagement.models.InvoiceStatus;
import com.aditya.solarmanagement.service.InvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {
	private final InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<MsedclInvoiceResponse>>> invoices(Authentication authentication,
			@RequestParam(required = false) String invoiceNo,
			@RequestParam(required = false) String consumerName,
			@RequestParam(required = false) String consumerNo,
			@RequestParam(required = false) InvoicePaymentStatus paymentStatus,
			@RequestParam(required = false) InvoiceStatus invoiceStatus,
			@RequestParam(required = false) LocalDate invoiceDateFrom,
			@RequestParam(required = false) LocalDate invoiceDateTo) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Invoices retrieved successfully",
				invoiceService.invoices(authentication.getName(), invoiceNo, consumerName, consumerNo, paymentStatus,
						invoiceStatus, invoiceDateFrom, invoiceDateTo)));
	}

	@PostMapping("/preview")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclInvoiceResponse>> preview(Authentication authentication,
			@Valid @RequestBody MsedclInvoiceRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Invoice preview calculated successfully",
				invoiceService.previewInvoice(authentication.getName(), request)));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclInvoiceResponse>> create(Authentication authentication,
			@Valid @RequestBody MsedclInvoiceRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED,
				"Invoice created successfully", invoiceService.createInvoice(authentication.getName(), request)));
	}

	@PostMapping("/{invoiceId}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> cancel(Authentication authentication, @PathVariable Long invoiceId) {
		invoiceService.cancelInvoice(authentication.getName(), invoiceId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Invoice cancelled successfully", null));
	}

	@PostMapping(value = "/{invoiceId}/mseb-bill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<MsedclInvoiceResponse>> uploadMsebBill(Authentication authentication,
			@PathVariable Long invoiceId, @RequestPart("file") MultipartFile file) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "MSEB bill uploaded successfully",
				invoiceService.uploadMsebBill(authentication.getName(), invoiceId, file)));
	}

	@GetMapping("/{invoiceId}/mseb-bill")
	public ResponseEntity<Resource> downloadMsebBill(Authentication authentication, @PathVariable Long invoiceId) {
		InvoiceService.InvoiceAttachmentDownload attachment = invoiceService.downloadMsebBill(authentication.getName(), invoiceId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(attachment.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(attachment.fileName(), StandardCharsets.UTF_8).build().toString())
				.header("X-Content-Type-Options", "nosniff")
				.body(attachment.resource());
	}

	@GetMapping("/{invoiceId}/payments")
	public ResponseEntity<ApiResponse<List<InvoicePaymentResponse>>> paymentHistory(Authentication authentication,
			@PathVariable Long invoiceId) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment history retrieved successfully",
				invoiceService.paymentHistory(authentication.getName(), invoiceId)));
	}

	@PostMapping("/{invoiceId}/payments")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<List<InvoicePaymentResponse>>> addPayment(Authentication authentication,
			@PathVariable Long invoiceId, @Valid @RequestBody InvoicePaymentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED,
				"Payment recorded successfully", invoiceService.addPayment(authentication.getName(), invoiceId, request)));
	}

	@PostMapping("/{invoiceId}/payments/{paymentId}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> cancelPayment(Authentication authentication,
			@PathVariable Long invoiceId, @PathVariable Long paymentId) {
		invoiceService.cancelPayment(authentication.getName(), invoiceId, paymentId);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Payment reversal recorded successfully", null));
	}
}