package com.aditya.solarmanagement.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

	public static final class EmployeeDetails implements UserDetails {
		private final Employee employee;

		EmployeeDetails(Employee employee) { this.employee = employee; }
		@Override public Collection<? extends GrantedAuthority> getAuthorities() {
			return List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
		}
		@Override public String getPassword() { return employee.getPasswordHash(); }
		@Override public String getUsername() { return employee.getEmailAddress(); }
		@Override public boolean isEnabled() { return employee.isActive(); }
		public boolean mustChangePassword() { return employee.mustChangePassword(); }
	}
}
