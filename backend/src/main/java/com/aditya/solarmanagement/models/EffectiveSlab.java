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
@Table(name = "effective_slabs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EffectiveSlab {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	private EffectiveSlabSchedule schedule;

	@Column(name = "up_to_units", precision = 14, scale = 4)
	private BigDecimal upToUnits;

	@Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal ratePerUnit;

	@Column(name = "adjustment_per_unit", nullable = false, precision = 12, scale = 4)
	private BigDecimal adjustmentPerUnit;

	public EffectiveSlab(EffectiveSlabSchedule schedule, BigDecimal upToUnits, BigDecimal ratePerUnit,
			BigDecimal adjustmentPerUnit) {
		this.schedule = schedule;
		this.upToUnits = upToUnits;
		this.ratePerUnit = ratePerUnit;
		this.adjustmentPerUnit = adjustmentPerUnit;
	}
}