package com.aditya.solarmanagement.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "msedcl_invoice_other_charges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MsedclInvoiceOtherCharge {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private MsedclInvoice invoice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reason_id", nullable = false)
	private OtherChargeReason reason;

	@Column(name = "reason_name_snapshot", nullable = false, length = 100)
	private String reasonName;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	public MsedclInvoiceOtherCharge(MsedclInvoice invoice, OtherChargeReason reason, BigDecimal amount) {
		this.invoice = invoice;
		this.reason = reason;
		this.reasonName = reason.getName();
		this.amount = amount;
	}
}
