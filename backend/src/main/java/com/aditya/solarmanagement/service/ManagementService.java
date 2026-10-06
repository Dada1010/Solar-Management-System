package com.aditya.solarmanagement.service;

import java.util.List;

import com.aditya.solarmanagement.dto.BranchRequest;
import com.aditya.solarmanagement.dto.BranchResponse;
import com.aditya.solarmanagement.dto.CompanyRequest;
import com.aditya.solarmanagement.dto.CompanyResponse;
import com.aditya.solarmanagement.dto.EmployeeRequest;
import com.aditya.solarmanagement.dto.EmployeeResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.models.Employee;

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
}
