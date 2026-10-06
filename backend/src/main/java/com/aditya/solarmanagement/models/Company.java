package com.aditya.solarmanagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 160)
	private String name;

	@Column(length = 120)
	private String industry;

	@Column(length = 255)
	private String address;

	@Column(length = 40)
	private String mobileNo;

	@Column(length = 160)
	private String emailAddress;

	public Company(String name, String industry, String address, String mobileNo, String emailAddress) {
		this.name = name;
		this.industry = industry;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
	}

	public void updateDetails(String name, String industry, String address, String mobileNo, String emailAddress) {
		this.name = name;
		this.industry = industry;
		this.address = address;
		this.mobileNo = mobileNo;
		this.emailAddress = emailAddress;
	}

}
