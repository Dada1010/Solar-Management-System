package com.aditya.solarmanagement.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "effective_rates", uniqueConstraints = {
		@UniqueConstraint(name = "uk_effective_rate_company_date", columnNames = { "company_id", "start_date" }),
		@UniqueConstraint(name = "uk_effective_rate_branch_date", columnNames = { "branch_id", "start_date" }),
		@UniqueConstraint(name = "uk_effective_rate_detail_date", columnNames = { "msedcl_detail_id", "start_date" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EffectiveRate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal ratePerUnit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "msedcl_detail_id")
	private MsedclDetail msedclDetail;

	@OneToMany(mappedBy = "effectiveRate", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<EffectiveRateSlab> slabs = new ArrayList<>();

	@Column(name = "fixed_charge", nullable = false, precision = 12, scale = 2)
	private BigDecimal fixedCharge = BigDecimal.ZERO;

	@Column(name = "wheeling_charge_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal wheelingChargePerUnit = BigDecimal.ZERO;

	@Column(name = "electricity_duty_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal electricityDutyPercent = BigDecimal.ZERO;

	@Column(name = "tax_on_sale_paise_per_unit", nullable = false, precision = 8, scale = 2)
	private BigDecimal taxOnSalePaisePerUnit = BigDecimal.ZERO;

	public record SlabSpec(BigDecimal upToUnits, BigDecimal ratePerUnit, BigDecimal adjustmentPerUnit) {}

	public EffectiveRate(LocalDate startDate, BigDecimal ratePerUnit, Company company, Branch branch,
			MsedclDetail msedclDetail) {
		this.startDate = startDate;
		this.ratePerUnit = ratePerUnit;
		this.company = company;
		this.branch = branch;
		this.msedclDetail = msedclDetail;
	}

	public void updateDetails(LocalDate startDate, BigDecimal ratePerUnit) {
		this.startDate = startDate;
		this.ratePerUnit = ratePerUnit;
	}

	public void replaceSlabs(List<SlabSpec> specs) {
		this.slabs.clear();
		for (SlabSpec spec : specs) {
			this.slabs.add(new EffectiveRateSlab(this, spec.upToUnits(), spec.ratePerUnit(), spec.adjustmentPerUnit()));
		}
	}

	public void applyTariffCharges(BigDecimal fixedCharge, BigDecimal wheelingChargePerUnit,
			BigDecimal electricityDutyPercent, BigDecimal taxOnSalePaisePerUnit) {
		this.fixedCharge = fixedCharge;
		this.wheelingChargePerUnit = wheelingChargePerUnit;
		this.electricityDutyPercent = electricityDutyPercent;
		this.taxOnSalePaisePerUnit = taxOnSalePaisePerUnit;
	}
}