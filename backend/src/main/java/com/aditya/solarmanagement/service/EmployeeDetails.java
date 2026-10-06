package com.aditya.solarmanagement.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.aditya.solarmanagement.models.Employee;

public class EmployeeDetails implements UserDetails {
	private final Employee employee;

	EmployeeDetails(Employee employee) {
		this.employee = employee;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
	}

	@Override
	public String getPassword() {
		return employee.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return employee.getEmailAddress();
	}

	@Override
	public boolean isEnabled() {
		return employee.isActive();
	}

	public boolean mustChangePassword() {
		return employee.mustChangePassword();
	}
}