package com.aditya.solarmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.solarmanagement.dto.ApiResponse;
import com.aditya.solarmanagement.dto.EmployeeRequest;
import com.aditya.solarmanagement.dto.EmployeeResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.service.ManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
	private final ManagementService managementService;

	public EmployeeController(ManagementService managementService) {
		this.managementService = managementService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> employees(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String name) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Employees retrieved successfully",
				managementService.employees(page, size, name)));
	}

	@GetMapping("/by-type/{employeeType}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> employeesByType(
			@PathVariable Employee.EmployeeType employeeType,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String name) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Employees retrieved successfully",
				managementService.employeesByType(employeeType, page, size, name)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<EmployeeResponse>> employee(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Employee retrieved successfully",
				managementService.employee(id)));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(@Valid @RequestBody EmployeeRequest request) {
		EmployeeResponse data = managementService.addEmployee(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED, "Employee created successfully", data));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable Long id,
			@Valid @RequestBody EmployeeRequest request) {
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Employee updated successfully",
				managementService.updateEmployee(id, request)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
		managementService.deleteEmployee(id);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Employee deleted successfully", null));
	}

	@PostMapping("/{id}/reset-password")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@PathVariable Long id) {
		MessageResponse data = managementService.resetEmployeePassword(id);
		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Password reset successfully", data));
	}
}