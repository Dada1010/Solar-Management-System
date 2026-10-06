package com.aditya.solarmanagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.LoginRequest;
import com.aditya.solarmanagement.dto.LoginResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.dto.PasswordChangeRequest;
import com.aditya.solarmanagement.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse data = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Signed in successfully", data));
	}

	@PostMapping("/change-password")
	public ResponseEntity<ApiResponse<MessageResponse>> changePassword(@AuthenticationPrincipal UserDetails principal,
			@Valid @RequestBody PasswordChangeRequest request) {
		MessageResponse data = authService.changePassword(principal.getUsername(), request);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data.message(), data));
	}

	@PostMapping("/password-reset-help")
	public ResponseEntity<ApiResponse<MessageResponse>> passwordResetHelp() {
		MessageResponse data = authService.passwordResetHelp();
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data.message(), data));
	}
}
