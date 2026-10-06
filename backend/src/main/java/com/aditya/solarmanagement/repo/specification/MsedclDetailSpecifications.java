package com.aditya.solarmanagement.repo.specification;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.aditya.solarmanagement.models.MsedclDetail;

public final class MsedclDetailSpecifications {
	private MsedclDetailSpecifications() { }

	public static Specification<MsedclDetail> byBranchId(Long branchId) {
		if (branchId == null) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		return (root, query, criteria) -> criteria.equal(root.get("customer").get("branch").get("id"), branchId);
	}

	public static Specification<MsedclDetail> byCustomerId(Long customerId) {
		if (customerId == null) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		return (root, query, criteria) -> criteria.equal(root.get("customer").get("id"), customerId);
	}

	public static Specification<MsedclDetail> byName(String name) {
		return contains("name", name);
	}

	public static Specification<MsedclDetail> byMobileNo(String mobileNo) {
		return contains("mobileNo", mobileNo);
	}

	public static Specification<MsedclDetail> byConsumerNo(String consumerNo) {
		return contains("consumerNo", consumerNo);
	}

	private static Specification<MsedclDetail> contains(String field, String value) {
		if (!StringUtils.hasText(value)) {
			return (root, query, criteria) -> criteria.conjunction();
		}
		String pattern = containsPattern(value);
		return (root, query, criteria) -> criteria.like(criteria.lower(root.get(field)), pattern, '\\');
	}

	private static String containsPattern(String value) {
		String escaped = value.trim().toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
		return "%" + escaped + "%";
	}
}