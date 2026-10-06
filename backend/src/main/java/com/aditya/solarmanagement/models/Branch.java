package com.aditya.solarmanagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "branches", uniqueConstraints = @UniqueConstraint(name = "uk_branch_company_name", columnNames = { "company_id", "name" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Branch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(length = 255)
	private String address;

	@Column(length = 40)
	private String mobileNo;

	@Column(length = 160)
	private String emailAddress;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	public Branch(String name, String address, String mobileNo, String emailAddress, Company company) {
		this.name = name;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
		this.company = company;
	}

	public void updateDetails(String name, String address, String mobileNo, String emailAddress, Company company) {
		this.name = name;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
		this.company = company;
	}

}
