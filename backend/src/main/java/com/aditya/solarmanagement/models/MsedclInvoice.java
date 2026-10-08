package com.aditya.solarmanagement.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "msedcl_invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MsedclInvoice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "invoice_no", nullable = false, length = 160)
	private String invoiceNo;

	@Column(name = "original_invoice_no_snapshot", length = 160)
	private String originalInvoiceNoSnapshot;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "msedcl_detail_id", nullable = false)
	private MsedclDetail msedclDetail;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reversal_of_invoice_id")
	private MsedclInvoice reversalOfInvoice;

	@Enumerated(EnumType.STRING)
	@Column(name = "invoice_status", nullable = false, length = 20)
	private InvoiceStatus status = InvoiceStatus.OPEN;

	@Column(name = "consumer_no_snapshot", nullable = false, length = 50)
	private String consumerNo;

	@Column(name = "consumer_name_snapshot", nullable = false, length = 100)
	private String consumerName;

	@Column(name = "billing_unit_snapshot", nullable = false, length = 50)
	private String billingUnit;

	@Enumerated(EnumType.STRING)
	@Column(name = "charge_type", nullable = false, length = 40)
	private MsedclChargeType chargeType;

	@Column(name = "invoice_date", nullable = false)
	private LocalDate invoiceDate;

	@Column(name = "due_days_snapshot", nullable = false)
	private int dueDays;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "billing_date", nullable = false)
	private LocalDate billingDate;

	@Column(name = "import_current", nullable = false, precision = 14, scale = 4)
	private BigDecimal importCurrent;

	@Column(name = "import_previous", nullable = false, precision = 14, scale = 4)
	private BigDecimal importPrevious;

	@Column(name = "import_consumption", nullable = false, precision = 14, scale = 4)
	private BigDecimal importConsumption;

	@Column(name = "export_current", nullable = false, precision = 14, scale = 4)
	private BigDecimal exportCurrent;

	@Column(name = "export_previous", nullable = false, precision = 14, scale = 4)
	private BigDecimal exportPrevious;

	@Column(name = "export_consumption", nullable = false, precision = 14, scale = 4)
	private BigDecimal exportConsumption;

	@Column(name = "generation_current", nullable = false, precision = 14, scale = 4)
	private BigDecimal generationCurrent;

	@Column(name = "generation_previous", nullable = false, precision = 14, scale = 4)
	private BigDecimal generationPrevious;

	@Column(name = "generation_consumption", nullable = false, precision = 14, scale = 4)
	private BigDecimal generationConsumption;

	@Column(name = "previous_bank_units", nullable = false, precision = 14, scale = 4)
	private BigDecimal previousBankUnits;

	@Column(name = "solar_offset_units", nullable = false, precision = 14, scale = 4)
	private BigDecimal solarOffsetUnits;

	@Column(name = "bank_solar_units", nullable = false, precision = 14, scale = 4)
	private BigDecimal bankSolarUnits;

	@Column(name = "solar_bill_units", nullable = false, precision = 14, scale = 4)
	private BigDecimal solarBillUnits;

	@Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal ratePerUnit;

	@Column(name = "rate_source", nullable = false, length = 40)
	private String rateSource;

	@Column(name = "solar_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal solarAmount;

	@Column(name = "mseb_bill_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal msebBillAmount;

	@Column(name = "invoice_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal invoiceAmount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referral_id")
	private Employee referral;

	@Column(name = "referral_percentage", precision = 5, scale = 2)
	private BigDecimal referralPercentage;

	@Column(name = "incentive_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal incentiveAmount = BigDecimal.ZERO;

	@Column(name = "other_charges_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal otherChargesAmount = BigDecimal.ZERO;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<MsedclInvoiceOtherCharge> otherCharges = new ArrayList<>();

	@Column(name = "total_consumption_units", precision = 14, scale = 4)
	private BigDecimal totalConsumptionUnits;

	@Column(name = "without_solar_bill_amount", precision = 14, scale = 2)
	private BigDecimal withoutSolarBillAmount;

	@Column(name = "with_solar_bill_amount", precision = 14, scale = 2)
	private BigDecimal withSolarBillAmount;

	@Column(name = "consumer_savings_amount", precision = 14, scale = 2)
	private BigDecimal consumerSavingsAmount;

	@Column(name = "mseb_bill_storage_name", length = 100)
	private String msebBillStorageName;

	@Column(name = "mseb_bill_file_name", length = 255)
	private String msebBillFileName;

	@Column(name = "mseb_bill_content_type", length = 100)
	private String msebBillContentType;

	@Column(name = "mseb_bill_uploaded_at")
	private LocalDateTime msebBillUploadedAt;

	public MsedclInvoice(MsedclDetail detail, LocalDate invoiceDate, LocalDate billingDate, LocalDate dueDate,
			BigDecimal importCurrent, BigDecimal importPrevious, BigDecimal importConsumption,
			BigDecimal exportCurrent, BigDecimal exportPrevious, BigDecimal exportConsumption,
			BigDecimal generationCurrent, BigDecimal generationPrevious, BigDecimal generationConsumption,
			BigDecimal previousBankUnits, BigDecimal solarOffsetUnits, BigDecimal bankSolarUnits,
			BigDecimal solarBillUnits,
			BigDecimal ratePerUnit, String rateSource, BigDecimal solarAmount, BigDecimal msebBillAmount,
			BigDecimal invoiceAmount, Employee referral, BigDecimal incentiveAmount, BigDecimal otherChargesAmount) {
		this.msedclDetail = detail;
		this.branch = detail.getCustomer().getBranch();
		this.company = branch.getCompany();
		this.invoiceNo = "PENDING-" + UUID.randomUUID();
		this.consumerNo = detail.getConsumerNo();
		this.consumerName = detail.getName();
		this.billingUnit = detail.getBillingUnit();
		this.chargeType = detail.getChargeType();
		this.invoiceDate = invoiceDate;
		this.dueDays = detail.getDueDays();
		this.dueDate = dueDate;
		this.billingDate = billingDate;
		this.importCurrent = importCurrent;
		this.importPrevious = importPrevious;
		this.importConsumption = importConsumption;
		this.exportCurrent = exportCurrent;
		this.exportPrevious = exportPrevious;
		this.exportConsumption = exportConsumption;
		this.generationCurrent = generationCurrent;
		this.generationPrevious = generationPrevious;
		this.generationConsumption = generationConsumption;
		this.previousBankUnits = previousBankUnits;
		this.solarOffsetUnits = solarOffsetUnits;
		this.bankSolarUnits = bankSolarUnits;
		this.solarBillUnits = solarBillUnits;
		this.ratePerUnit = ratePerUnit;
		this.rateSource = rateSource;
		this.solarAmount = solarAmount;
		this.msebBillAmount = msebBillAmount;
		this.invoiceAmount = invoiceAmount;
		this.referral = referral;
		this.referralPercentage = referral == null ? null : referral.getReferralPercentage();
		this.incentiveAmount = incentiveAmount;
		this.otherChargesAmount = otherChargesAmount;
	}

	public void addOtherCharge(OtherChargeReason reason, BigDecimal amount) {
		this.otherCharges.add(new MsedclInvoiceOtherCharge(this, reason, amount));
	}

	public void applySavings(BigDecimal totalConsumptionUnits, BigDecimal withoutSolarBillAmount,
			BigDecimal withSolarBillAmount, BigDecimal consumerSavingsAmount) {
		this.totalConsumptionUnits = totalConsumptionUnits;
		this.withoutSolarBillAmount = withoutSolarBillAmount;
		this.withSolarBillAmount = withSolarBillAmount;
		this.consumerSavingsAmount = consumerSavingsAmount;
	}

	public void assignInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public void cancel() {
		this.status = InvoiceStatus.CANCELLED;
	}

	public MsedclInvoice createCancellationReversal(LocalDate cancellationDate) {
		MsedclInvoice reversal = new MsedclInvoice();
		reversal.msedclDetail = this.msedclDetail;
		reversal.company = this.company;
		reversal.branch = this.branch;
		reversal.reversalOfInvoice = this;
		reversal.originalInvoiceNoSnapshot = this.invoiceNo;
		reversal.status = InvoiceStatus.CANCELLED;
		reversal.invoiceNo = "PENDING-" + UUID.randomUUID();
		reversal.consumerNo = this.consumerNo;
		reversal.consumerName = this.consumerName;
		reversal.billingUnit = this.billingUnit;
		reversal.chargeType = this.chargeType;
		reversal.invoiceDate = cancellationDate;
		reversal.dueDays = 0;
		reversal.dueDate = cancellationDate;
		reversal.billingDate = this.billingDate;
		reversal.importCurrent = this.importCurrent;
		reversal.importPrevious = this.importPrevious;
		reversal.importConsumption = this.importConsumption;
		reversal.exportCurrent = this.exportCurrent;
		reversal.exportPrevious = this.exportPrevious;
		reversal.exportConsumption = this.exportConsumption;
		reversal.generationCurrent = this.generationCurrent;
		reversal.generationPrevious = this.generationPrevious;
		reversal.generationConsumption = this.generationConsumption;
		reversal.previousBankUnits = this.previousBankUnits;
		reversal.solarOffsetUnits = this.solarOffsetUnits;
		reversal.bankSolarUnits = this.bankSolarUnits;
		reversal.solarBillUnits = this.solarBillUnits;
		reversal.ratePerUnit = this.ratePerUnit;
		reversal.rateSource = this.rateSource;
		reversal.solarAmount = this.solarAmount.negate();
		reversal.msebBillAmount = this.msebBillAmount.negate();
		reversal.invoiceAmount = this.invoiceAmount.negate();
		reversal.referral = this.referral;
		reversal.referralPercentage = this.referralPercentage;
		reversal.incentiveAmount = this.incentiveAmount.negate();
		reversal.otherChargesAmount = this.otherChargesAmount.negate();
		reversal.totalConsumptionUnits = this.totalConsumptionUnits;
		reversal.withoutSolarBillAmount = this.withoutSolarBillAmount == null ? null : this.withoutSolarBillAmount.negate();
		reversal.withSolarBillAmount = this.withSolarBillAmount == null ? null : this.withSolarBillAmount.negate();
		reversal.consumerSavingsAmount = this.consumerSavingsAmount == null ? null : this.consumerSavingsAmount.negate();
		for (MsedclInvoiceOtherCharge charge : this.otherCharges) {
			reversal.addOtherCharge(charge.getReason(), charge.getAmount().negate());
		}
		return reversal;
}

	public void attachMsebBill(String storageName, String fileName, String contentType, LocalDateTime uploadedAt) {
		this.msebBillStorageName = storageName;
		this.msebBillFileName = fileName;
		this.msebBillContentType = contentType;
		this.msebBillUploadedAt = uploadedAt;
	}
}