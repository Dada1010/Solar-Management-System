package com.aditya.solarmanagement.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.EffectiveRate;

public interface EffectiveRateRepository extends JpaRepository<EffectiveRate, Long> {
	boolean existsByCompany_Id(Long companyId);
	boolean existsByBranch_Id(Long branchId);
	List<EffectiveRate> findAllByCompany_IdOrderByStartDateDescIdDesc(Long companyId);
	List<EffectiveRate> findAllByBranch_IdOrderByStartDateDescIdDesc(Long branchId);
	List<EffectiveRate> findAllByMsedclDetail_IdOrderByStartDateDescIdDesc(Long detailId);

	boolean existsByCompany_IdAndStartDate(Long companyId, LocalDate startDate);
	boolean existsByCompany_IdAndStartDateAndIdNot(Long companyId, LocalDate startDate, Long id);
	boolean existsByBranch_IdAndStartDate(Long branchId, LocalDate startDate);
	boolean existsByBranch_IdAndStartDateAndIdNot(Long branchId, LocalDate startDate, Long id);
	boolean existsByMsedclDetail_IdAndStartDate(Long detailId, LocalDate startDate);
	boolean existsByMsedclDetail_IdAndStartDateAndIdNot(Long detailId, LocalDate startDate, Long id);
}