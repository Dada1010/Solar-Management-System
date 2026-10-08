package com.aditya.solarmanagement.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.OtherChargeReason;

public interface OtherChargeReasonRepository extends JpaRepository<OtherChargeReason, Long> {
	boolean existsByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	List<OtherChargeReason> findAllByOrderByNameAsc();
}
