package com.aditya.solarmanagement.repo.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.aditya.solarmanagement.models.InvoicePaymentStatus;
import com.aditya.solarmanagement.models.InvoiceStatus;
import com.aditya.solarmanagement.models.MsedclInvoice;
import com.aditya.solarmanagement.models.MsedclInvoicePayment;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class InvoiceSpecifications {
	private InvoiceSpecifications() { }

	public static Specification<MsedclInvoice> byBranchId(Long branchId) {
		if (branchId == null) return alwaysTrue();
		return (root, query, criteria) -> criteria.equal(root.get("branch").get("id"), branchId);
	}

	public static Specification<MsedclInvoice> byCustomerId(Long customerId) {
		if (customerId == null) return alwaysTrue();
		return (root, query, criteria) -> criteria.equal(root.get("msedclDetail").get("customer").get("id"), customerId);
	}

	public static Specification<MsedclInvoice> byInvoiceNo(String invoiceNo) {
		return contains("invoiceNo", invoiceNo);
	}

	public static Specification<MsedclInvoice> byConsumerName(String consumerName) {
		return contains("consumerName", consumerName);
	}

	public static Specification<MsedclInvoice> byConsumerNo(String consumerNo) {
		return contains("consumerNo", consumerNo);
	}

	public static Specification<MsedclInvoice> invoiceDateFrom(LocalDate date) {
		if (date == null) return alwaysTrue();
		return (root, query, criteria) -> criteria.greaterThanOrEqualTo(root.get("invoiceDate"), date);
	}

	public static Specification<MsedclInvoice> invoiceDateTo(LocalDate date) {
		if (date == null) return alwaysTrue();
		return (root, query, criteria) -> criteria.lessThanOrEqualTo(root.get("invoiceDate"), date);
	}

	public static Specification<MsedclInvoice> paymentStatus(InvoicePaymentStatus status) {
		if (status == null) return alwaysTrue();
		return (root, query, criteria) -> {
			Subquery<BigDecimal> paidTotal = query.subquery(BigDecimal.class);
			Root<MsedclInvoicePayment> payment = paidTotal.from(MsedclInvoicePayment.class);
			paidTotal.select(criteria.coalesce(criteria.sum(payment.<BigDecimal>get("amount")), BigDecimal.ZERO));
			paidTotal.where(criteria.equal(payment.get("invoice").get("id"), root.get("id")));
			Expression<BigDecimal> balance = criteria.diff(root.<BigDecimal>get("invoiceAmount"), paidTotal);
			return criteria.and(criteria.equal(root.get("status"), InvoiceStatus.OPEN),
					status == InvoicePaymentStatus.PAID
							? criteria.lessThanOrEqualTo(balance, BigDecimal.ZERO)
							: criteria.greaterThan(balance, BigDecimal.ZERO));
		};
	}

	public static Specification<MsedclInvoice> byStatus(InvoiceStatus status) {
		if (status == null) return alwaysTrue();
		return (root, query, criteria) -> criteria.equal(root.get("status"), status);
	}

	private static Specification<MsedclInvoice> contains(String field, String value) {
		if (!StringUtils.hasText(value)) return alwaysTrue();
		String pattern = "%" + value.trim().toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
		return (root, query, criteria) -> criteria.like(criteria.lower(root.get(field)), pattern, '\\');
	}

	private static Specification<MsedclInvoice> alwaysTrue() {
		return (root, query, criteria) -> criteria.conjunction();
	}
}