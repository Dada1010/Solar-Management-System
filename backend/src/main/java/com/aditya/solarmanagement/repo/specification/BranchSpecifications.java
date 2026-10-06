package com.aditya.solarmanagement.repo.specification;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.aditya.solarmanagement.models.Branch;

public final class BranchSpecifications {
	private BranchSpecifications() { }

	public static Specification<Branch> byName(String name) {
		if (!StringUtils.hasText(name)) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		String pattern = containsPattern(name);
		return (root, query, criteria) -> criteria.like(criteria.lower(root.get("name")), pattern, '\\');
	}

	public static Specification<Branch> byCompanyId(Long companyId) {
		if (companyId == null) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		return (root, query, criteria) -> criteria.equal(root.get("company").get("id"), companyId);
	}

	private static String containsPattern(String value) {
		String escaped = value.trim().toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
		return "%" + escaped + "%";
	}
}