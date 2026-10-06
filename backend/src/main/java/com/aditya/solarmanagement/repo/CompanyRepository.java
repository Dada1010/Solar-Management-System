package com.aditya.solarmanagement.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
	boolean existsByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
