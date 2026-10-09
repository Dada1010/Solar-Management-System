package com.aditya.solarmanagement.service;

import java.util.List;

import com.aditya.solarmanagement.dto.BranchRequest;
import com.aditya.solarmanagement.dto.BranchResponse;
import com.aditya.solarmanagement.dto.CompanyRequest;
import com.aditya.solarmanagement.dto.CompanyResponse;
import com.aditya.solarmanagement.dto.EmployeeRequest;
import com.aditya.solarmanagement.dto.EmployeeResponse;
import com.aditya.solarmanagement.dto.EffectiveRateRequest;
import com.aditya.solarmanagement.dto.EffectiveRateResponse;
import com.aditya.solarmanagement.dto.EffectiveSlabScheduleRequest;
import com.aditya.solarmanagement.dto.EffectiveSlabScheduleResponse;
import com.aditya.solarmanagement.dto.MsedclDetailRequest;
import com.aditya.solarmanagement.dto.MsedclDetailResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EffectiveRateOwnerType;

public interface ManagementService {
	PageResponse<CompanyResponse> companies(int page, int size);
	CompanyResponse company(Long id);
	CompanyResponse addCompany(CompanyRequest request);
	CompanyResponse updateCompany(Long id, CompanyRequest request);
	void deleteCompany(Long id);
	PageResponse<BranchResponse> branches(int page, int size, String name);
	BranchResponse branch(Long id);
	PageResponse<BranchResponse> branchesForCompany(Long companyId, int page, int size, String name);
	BranchResponse addBranch(BranchRequest request);
	BranchResponse updateBranch(Long id, BranchRequest request);
	void deleteBranch(Long id);
	PageResponse<EmployeeResponse> employees(int page, int size, String name);
	PageResponse<EmployeeResponse> employeesByType(Employee.EmployeeType employeeType, int page, int size, String name);
	EmployeeResponse currentEmployee(String requestingEmail);
	EmployeeResponse employee(Long id);
	EmployeeResponse addEmployee(EmployeeRequest request);
	EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
	void deleteEmployee(Long id);
	MessageResponse resetEmployeePassword(Long id);
	List<MsedclDetailResponse> consumerDetails(String requestingEmail, String name, String mobileNo, String consumerNo);
	List<MsedclDetailResponse> msedclDetails(Long customerId, String requestingEmail, String name, String mobileNo,
			String consumerNo);
	MsedclDetailResponse addMsedclDetail(String requestingEmail, Long customerId, MsedclDetailRequest request);
	MsedclDetailResponse updateMsedclDetail(String requestingEmail, Long customerId, Long detailId,
			MsedclDetailRequest request);
	MsedclDetailResponse updateConsumerDetail(String requestingEmail, Long detailId, MsedclDetailRequest request);
	void deleteMsedclDetail(String requestingEmail, Long customerId, Long detailId);
	List<EffectiveRateResponse> effectiveRates(EffectiveRateOwnerType ownerType, Long ownerId, String requestingEmail);
	EffectiveRateResponse addEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, EffectiveRateRequest request);
	EffectiveRateResponse updateEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId,
			EffectiveRateRequest request);
	void deleteEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId);
	List<EffectiveSlabScheduleResponse> effectiveSlabSchedules(EffectiveRateOwnerType ownerType, Long ownerId,
			String requestingEmail);
	EffectiveSlabScheduleResponse addEffectiveSlabSchedule(EffectiveRateOwnerType ownerType, Long ownerId,
			String requestingEmail, EffectiveSlabScheduleRequest request);
	EffectiveSlabScheduleResponse updateEffectiveSlabSchedule(EffectiveRateOwnerType ownerType, Long ownerId,
			Long scheduleId, String requestingEmail, EffectiveSlabScheduleRequest request);
	void deleteEffectiveSlabSchedule(EffectiveRateOwnerType ownerType, Long ownerId, Long scheduleId,
			String requestingEmail);
}
