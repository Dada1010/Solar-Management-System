package com.aditya.solarmanagement.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {
	public enum EmployeeType { CUSTOMER, COMPANY_EMPLOYEE, REFERRAL }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String firstName;

	@Column(nullable = false, length = 100)
	private String lastName;

	@Column(length = 255)
	private String address;

	@Column(length = 40)
	private String mobileNo;

	@Column(nullable = false, unique = true, length = 160)
	private String emailAddress;

	@Column(nullable = false, length = 100)
	@Setter
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EmployeeType employeeType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EmployeeRole role = EmployeeRole.USER;

	@Column(nullable = false)
	@Setter
	private boolean mustChangePassword = true;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "referral_percentage", precision = 5, scale = 2)
	private BigDecimal referralPercentage;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<MsedclDetail> msedclDetails = new ArrayList<>();

	public Employee(String firstName, String lastName, String address, String mobileNo, String emailAddress,
			String passwordHash, EmployeeType employeeType, EmployeeRole role, Branch branch,
			BigDecimal referralPercentage) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
		this.passwordHash = passwordHash;
		this.employeeType = employeeType;
		this.role = role;
		this.branch = branch;
		applyReferralSettings(employeeType, referralPercentage);
	}

	public void updateDetails(String firstName, String lastName, String address, String mobileNo,
			String emailAddress, EmployeeType employeeType, EmployeeRole role, Branch branch,
			BigDecimal referralPercentage) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
		this.employeeType = employeeType;
		this.role = role;
		this.branch = branch;
		applyReferralSettings(employeeType, referralPercentage);
	}

	// Referral partners earn incentives but never sign in.
	private void applyReferralSettings(EmployeeType employeeType, BigDecimal referralPercentage) {
		boolean referral = employeeType == EmployeeType.REFERRAL;
		this.referralPercentage = referral ? referralPercentage : null;
		this.active = !referral;
	}

	public void replaceMsedclDetails(List<MsedclDetail> details) {
		this.msedclDetails.clear();
		for (MsedclDetail detail : details) {
			detail.setCustomer(this);
			this.msedclDetails.add(detail);
		}
	}

	public void addMsedclDetail(MsedclDetail detail) {
		detail.setCustomer(this);
		this.msedclDetails.add(detail);
	}

	public boolean removeMsedclDetail(MsedclDetail detail) {
		return this.msedclDetails.remove(detail);
	}

	public boolean mustChangePassword() { return mustChangePassword; }
}
