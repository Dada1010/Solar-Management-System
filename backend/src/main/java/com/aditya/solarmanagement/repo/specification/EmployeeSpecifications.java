package com.aditya.solarmanagement.repo.specification;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.aditya.solarmanagement.models.Employee;

public final class EmployeeSpecifications {
	private EmployeeSpecifications() { }

	public static Specification<Employee> byName(String name) {
		if (!StringUtils.hasText(name)) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		String pattern = containsPattern(name);
		return (root, query, criteria) -> criteria.or(
				criteria.like(criteria.lower(root.get("firstName")), pattern, '\\'),
				criteria.like(criteria.lower(root.get("lastName")), pattern, '\\'),
				criteria.like(criteria.lower(criteria.concat(
						criteria.concat(root.get("firstName"), " "), root.get("lastName"))), pattern, '\\'));
	}

	public static Specification<Employee> byType(Employee.EmployeeType employeeType) {
		if (employeeType == null) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		return (root, query, criteria) -> criteria.equal(root.get("employeeType"), employeeType);
	}

	private static String containsPattern(String value) {
		String escaped = value.trim().toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
		return "%" + escaped + "%";
	}
}