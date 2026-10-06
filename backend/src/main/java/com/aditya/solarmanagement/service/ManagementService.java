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
import com.aditya.solarmanagement.dto.MsedclDetailRequest;
import com.aditya.solarmanagement.dto.MsedclDetailResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EffectiveRateOwnerType;

public interface ManagementService {
	PageResponse<CompanyResponse> companies(int page, int size);
	CompanyResponse company(Long id);
	CompanyResponse addCompany(CompanyRequest request);
	CompanyResponse updateCompany(Long id, CompanyRequest request);
	PageResponse<BranchResponse> branches(int page, int size, String name);
	BranchResponse branch(Long id);
	PageResponse<BranchResponse> branchesForCompany(Long companyId, int page, int size, String name);
	BranchResponse addBranch(BranchRequest request);
	BranchResponse updateBranch(Long id, BranchRequest request);
	PageResponse<EmployeeResponse> employees(int page, int size, String name);
	PageResponse<EmployeeResponse> employeesByType(Employee.EmployeeType employeeType, int page, int size, String name);
	EmployeeResponse employee(Long id);
	EmployeeResponse addEmployee(EmployeeRequest request);
	EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
	List<MsedclDetailResponse> consumerDetails(String requestingEmail, String name, String mobileNo, String consumerNo);
	List<MsedclDetailResponse> msedclDetails(Long customerId, String requestingEmail, String name, String mobileNo,
			String consumerNo);
	MsedclDetailResponse addMsedclDetail(Long customerId, MsedclDetailRequest request);
	MsedclDetailResponse updateMsedclDetail(Long customerId, Long detailId, MsedclDetailRequest request);
	void deleteMsedclDetail(Long customerId, Long detailId);
	List<EffectiveRateResponse> effectiveRates(EffectiveRateOwnerType ownerType, Long ownerId);
	EffectiveRateResponse addEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, EffectiveRateRequest request);
	EffectiveRateResponse updateEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId,
			EffectiveRateRequest request);
	void deleteEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId);
}
