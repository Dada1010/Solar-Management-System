package com.aditya.solarmanagement.service.impl;

import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.dto.LoginRequest;
import com.aditya.solarmanagement.dto.LoginResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.dto.PasswordChangeRequest;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.repo.EmployeeRepository;
import com.aditya.solarmanagement.service.AuthService;
import com.aditya.solarmanagement.utility.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
	private final EmployeeRepository employees;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthServiceImpl(EmployeeRepository employees, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.employees = employees;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Override
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		logger.debug("Processing login request");
		Employee employee = employees.findByEmailAddressIgnoreCase(request.emailAddress())
				.filter(Employee::isActive)
				.filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
		var company = employee.getBranch().getCompany();
		logger.info("Login successful for employee id={}", employee.getId());
		return new LoginResponse(jwtService.issue(employee), "Bearer", jwtService.expirationSeconds(),
				employee.getEmailAddress(), employee.getFirstName() + " " + employee.getLastName(), employee.getRole().name(),
				employee.mustChangePassword(), company.getId(), company.getName());
	}

	@Override
	@Transactional
	public MessageResponse changePassword(String emailAddress, PasswordChangeRequest request) {
		logger.debug("Processing password change request");
		Employee employee = employees.findByEmailAddressIgnoreCase(emailAddress)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
		if (!passwordEncoder.matches(request.currentPassword(), employee.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
		}
		if (passwordEncoder.matches(request.newPassword(), employee.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a password different from the current one");
		}
		employee.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		employee.setMustChangePassword(false);
		employees.save(employee);
		logger.info("Password changed for employee id={}", employee.getId());
		return new MessageResponse("Password updated");
	}

	@Override
	public MessageResponse passwordResetHelp() {
		logger.info("Password reset help requested");
		return new MessageResponse("Please contact your workspace administrator to reset your password.");
	}
}
