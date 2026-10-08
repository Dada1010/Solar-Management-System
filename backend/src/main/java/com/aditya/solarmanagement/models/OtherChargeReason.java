package com.aditya.solarmanagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "other_charge_reasons", uniqueConstraints = {
		@UniqueConstraint(name = "uk_other_charge_reason_name", columnNames = "name") })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtherChargeReason {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	public OtherChargeReason(String name) {
		this.name = name;
	}

	public void rename(String name) {
		this.name = name;
	}
}
