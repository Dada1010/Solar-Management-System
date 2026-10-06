package com.aditya.solarmanagement.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_msedcl_details", uniqueConstraints = {
		@UniqueConstraint(name = "uk_customer_msedcl_consumer_no", columnNames = "consumer_no") })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MsedclDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "billing_unit", nullable = false, length = 50)
	private String billingUnit;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "mobile_no", nullable = false, length = 40)
	private String mobileNo;

	@Column(name = "consumer_no", nullable = false, length = 50)
	private String consumerNo;

	@Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal ratePerUnit;

	@Column(name = "last_invoice_no", length = 160)
	private String lastInvoiceNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "charge_type", nullable = false, length = 40)
	private MsedclChargeType chargeType = MsedclChargeType.ONLY_SOLAR_GENERATION;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Employee customer;

	public MsedclDetail(String billingUnit, String name, String mobileNo, String consumerNo, BigDecimal ratePerUnit,
			MsedclChargeType chargeType) {
		this.billingUnit = billingUnit;
		this.name = name;
		this.mobileNo = mobileNo;
		this.consumerNo = consumerNo;
		this.ratePerUnit = ratePerUnit;
		this.chargeType = chargeType == null ? MsedclChargeType.ONLY_SOLAR_GENERATION : chargeType;
	}

	public void updateDetails(String billingUnit, String name, String mobileNo, String consumerNo,
			BigDecimal ratePerUnit, MsedclChargeType chargeType) {
		this.billingUnit = billingUnit;
		this.name = name;
		this.mobileNo = mobileNo;
		this.consumerNo = consumerNo;
		this.ratePerUnit = ratePerUnit;
		this.chargeType = chargeType == null ? MsedclChargeType.ONLY_SOLAR_GENERATION : chargeType;
	}

	public void setCustomer(Employee customer) {
		this.customer = customer;
	}

	public void updateLastInvoiceNo(String invoiceNo) {
		this.lastInvoiceNo = invoiceNo;
	}
}