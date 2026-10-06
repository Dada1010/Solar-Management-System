package com.aditya.solarmanagement.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.repo.EmployeeRepository;

@Service
public class EmployeeDetailsService implements UserDetailsService {
	private final EmployeeRepository employees;

	public EmployeeDetailsService(EmployeeRepository employees) {
		this.employees = employees;
	}

	@Override
	public EmployeeDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
		Employee employee = employees.findByEmailAddressIgnoreCase(emailAddress)
				.orElseThrow(() -> new UsernameNotFoundException("Account not found"));
		return new EmployeeDetails(employee);
	}

}
