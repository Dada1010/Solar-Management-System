package com.aditya.solarmanagement.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.EffectiveSlabSchedule;

public interface EffectiveSlabScheduleRepository extends JpaRepository<EffectiveSlabSchedule, Long> {
	List<EffectiveSlabSchedule> findAllByCompany_IdOrderByStartDateDescIdDesc(Long companyId);
	List<EffectiveSlabSchedule> findAllByBranch_IdOrderByStartDateDescIdDesc(Long branchId);
	List<EffectiveSlabSchedule> findAllByMsedclDetail_IdOrderByStartDateDescIdDesc(Long detailId);

	boolean existsByCompany_IdAndStartDate(Long companyId, LocalDate startDate);
	boolean existsByCompany_IdAndStartDateAndIdNot(Long companyId, LocalDate startDate, Long id);
	boolean existsByBranch_IdAndStartDate(Long branchId, LocalDate startDate);
	boolean existsByBranch_IdAndStartDateAndIdNot(Long branchId, LocalDate startDate, Long id);
	boolean existsByMsedclDetail_IdAndStartDate(Long detailId, LocalDate startDate);
	boolean existsByMsedclDetail_IdAndStartDateAndIdNot(Long detailId, LocalDate startDate, Long id);
}