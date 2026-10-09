package com.aditya.solarmanagement.models;

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
@Table(name = "effective_slab_schedules", uniqueConstraints = {
		@UniqueConstraint(name = "uk_slab_schedule_company_date", columnNames = { "company_id", "start_date" }),
		@UniqueConstraint(name = "uk_slab_schedule_branch_date", columnNames = { "branch_id", "start_date" }),
		@UniqueConstraint(name = "uk_slab_schedule_detail_date", columnNames = { "msedcl_detail_id", "start_date" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EffectiveSlabSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "msedcl_detail_id")
	private MsedclDetail msedclDetail;

	@OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	@BatchSize(size = 50)
	private List<EffectiveSlab> slabs = new ArrayList<>();

	public record SlabSpec(java.math.BigDecimal upToUnits, java.math.BigDecimal ratePerUnit,
			java.math.BigDecimal adjustmentPerUnit) {}

	public EffectiveSlabSchedule(LocalDate startDate, Company company, Branch branch, MsedclDetail msedclDetail) {
		this.startDate = startDate;
		this.company = company;
		this.branch = branch;
		this.msedclDetail = msedclDetail;
	}

	public void updateStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void replaceSlabs(List<SlabSpec> specs) {
		this.slabs.clear();
		for (SlabSpec spec : specs) {
			this.slabs.add(new EffectiveSlab(this, spec.upToUnits(), spec.ratePerUnit(), spec.adjustmentPerUnit()));
		}
	}
}