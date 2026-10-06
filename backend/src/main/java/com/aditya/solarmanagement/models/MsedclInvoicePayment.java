package com.aditya.solarmanagement.models;

import java.math.BigDecimal;
import java.time.LocalDate;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "msedcl_invoice_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MsedclInvoicePayment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private MsedclInvoice invoice;

	@Column(name = "payment_date", nullable = false)
	private LocalDate paymentDate;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_type", nullable = false, length = 20)
	private InvoicePaymentType paymentType;

	@Column(name = "transaction_no", length = 100)
	private String transactionNo;

	@Column(length = 255)
	private String note;

	public MsedclInvoicePayment(MsedclInvoice invoice, LocalDate paymentDate, BigDecimal amount,
			InvoicePaymentType paymentType, String transactionNo, String note) {
		this.invoice = invoice;
		this.paymentDate = paymentDate;
		this.amount = amount;
		this.paymentType = paymentType;
		this.transactionNo = transactionNo;
		this.note = note;
	}
}