package com.aditya.solarmanagement.service.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.dto.BranchRequest;
import com.aditya.solarmanagement.dto.BranchResponse;
import com.aditya.solarmanagement.dto.CompanyRequest;
import com.aditya.solarmanagement.dto.CompanyResponse;
import com.aditya.solarmanagement.dto.EmployeeRequest;
import com.aditya.solarmanagement.dto.EmployeeResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.models.Branch;
import com.aditya.solarmanagement.models.Company;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EmployeeRole;
import com.aditya.solarmanagement.repo.BranchRepository;
import com.aditya.solarmanagement.repo.CompanyRepository;
import com.aditya.solarmanagement.repo.EmployeeRepository;
import com.aditya.solarmanagement.repo.specification.BranchSpecifications;
import com.aditya.solarmanagement.repo.specification.EmployeeSpecifications;
import com.aditya.solarmanagement.service.ManagementService;

@Service
@Transactional(readOnly = true)
public class ManagementServiceImpl implements ManagementService {
	private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class);
	private final CompanyRepository companies;
	private final BranchRepository branches;
	private final EmployeeRepository employees;
	private final PasswordEncoder passwordEncoder;

	public ManagementServiceImpl(CompanyRepository companies, BranchRepository branches, EmployeeRepository employees,
			PasswordEncoder passwordEncoder) {
		this.companies = companies;
		this.branches = branches;
		this.employees = employees;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public PageResponse<CompanyResponse> companies(int page, int size) {
		logger.debug("Listing companies page={} size={}", page, size);
		Page<Company> companyPage = companies.findAll(pageable(page, size));
		List<Long> companyIds = companyPage.getContent().stream().map(Company::getId).toList();
		Map<Long, Long> branchCounts = new HashMap<>();
		if (!companyIds.isEmpty()) {
			for (Object[] row : branches.countBranchesByCompanyIds(companyIds)) {
				branchCounts.put((Long) row[0], ((Number) row[1]).longValue());
			}
		}
		Page<CompanyResponse> result = companyPage.map(company ->
				companyResponse(company, branchCounts.getOrDefault(company.getId(), 0L)));
		return PageResponse.from(result);
	}

	@Override
	public CompanyResponse company(Long id) {
		logger.debug("Retrieving company id={}", id);
		return companyResponse(companies.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found")));
	}

	@Override
	@Transactional
	public CompanyResponse addCompany(CompanyRequest request) {
		logger.debug("Creating company");
		if (companies.existsByNameIgnoreCase(request.name().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A company with this name already exists");
		}
		Company company = companies.save(new Company(request.name().trim(), request.industry(), request.address(),
				request.mobileNo(), request.emailAddress()));
		logger.info("Created company id={}", company.getId());
		return companyResponse(company);
	}

	@Override
	@Transactional
	public CompanyResponse updateCompany(Long id, CompanyRequest request) {
		logger.debug("Updating company id={}", id);
		Company company = companies.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
		if (companies.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A company with this name already exists");
		}
		company.updateDetails(request.name().trim(), request.industry(), request.address(), request.mobileNo(),
				request.emailAddress());
		Company updatedCompany = companies.save(company);
		logger.info("Updated company id={}", id);
		return companyResponse(updatedCompany);
	}

	@Override
	public PageResponse<BranchResponse> branches(int page, int size, String name) {
		logger.debug("Listing branches page={} size={} nameFilterPresent={}", page, size, name != null && !name.isBlank());
		Page<BranchResponse> result = branches.findAll(BranchSpecifications.byName(name), pageable(page, size))
				.map(this::branchResponse);
		return PageResponse.from(result);
	}

	@Override
	public BranchResponse branch(Long id) {
		logger.debug("Retrieving branch id={}", id);
		return branchResponse(branches.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found")));
	}

	@Override
	public PageResponse<BranchResponse> branchesForCompany(Long companyId, int page, int size, String name) {
		logger.debug("Listing branches for company id={} page={} size={} nameFilterPresent={}", companyId, page, size,
				name != null && !name.isBlank());
		if (!companies.existsById(companyId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
		}
		Page<BranchResponse> result = branches.findAll(
				BranchSpecifications.byCompanyId(companyId).and(BranchSpecifications.byName(name)), pageable(page, size))
				.map(this::branchResponse);
		return PageResponse.from(result);
	}

	@Override
	@Transactional
	public BranchResponse addBranch(BranchRequest request) {
		logger.debug("Creating branch for company id={}", request.companyId());
		Company company = companies.findById(request.companyId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
		Branch branch = branches.save(new Branch(request.name().trim(), request.address(), request.mobileNo(),
				request.emailAddress(), company));
		logger.info("Created branch id={} for company id={}", branch.getId(), company.getId());
		return branchResponse(branch);
	}

	@Override
	@Transactional
	public BranchResponse updateBranch(Long id, BranchRequest request) {
		logger.debug("Updating branch id={}", id);
		Branch branch = branches.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
		Company company = companies.findById(request.companyId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
		if (branches.existsByCompanyIdAndNameIgnoreCaseAndIdNot(company.getId(), request.name().trim(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A branch with this name already exists for this company");
		}
		branch.updateDetails(request.name().trim(), request.address(), request.mobileNo(), request.emailAddress(), company);
		Branch updatedBranch = branches.save(branch);
		logger.info("Updated branch id={} for company id={}", id, company.getId());
		return branchResponse(updatedBranch);
	}

	@Override
	public PageResponse<EmployeeResponse> employees(int page, int size, String name) {
		logger.debug("Listing employees page={} size={} nameFilterPresent={}", page, size,
				name != null && !name.isBlank());
		Page<EmployeeResponse> result = employees.findAll(EmployeeSpecifications.byName(name), pageable(page, size))
				.map(this::employeeResponse);
		return PageResponse.from(result);
	}

	@Override
	public PageResponse<EmployeeResponse> employeesByType(Employee.EmployeeType employeeType, int page, int size,
			String name) {
		logger.debug("Listing employees by type={} page={} size={} nameFilterPresent={}", employeeType, page, size,
				name != null && !name.isBlank());
		Page<EmployeeResponse> result = employees.findAll(
				EmployeeSpecifications.byType(employeeType).and(EmployeeSpecifications.byName(name)), pageable(page, size))
				.map(this::employeeResponse);
		return PageResponse.from(result);
	}

	@Override
	public EmployeeResponse employee(Long id) {
		logger.debug("Retrieving employee id={}", id);
		return employeeResponse(employees.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found")));
	}

	@Override
	@Transactional
	public EmployeeResponse addEmployee(EmployeeRequest request) {
		logger.debug("Creating employee for branch id={}", request.branchId());
		if (employees.existsByEmailAddressIgnoreCase(request.emailAddress().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
		}
		Branch branch = branches.findById(request.branchId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
		EmployeeRole role = request.employeeType() == Employee.EmployeeType.CUSTOMER
				? EmployeeRole.CUSTOMER
				: EmployeeRole.USER;
		Employee employee = employees.save(new Employee(request.firstName().trim(), request.lastName().trim(),
				request.address(), request.mobileNo(), request.emailAddress().trim().toLowerCase(),
				passwordEncoder.encode("123456"), request.employeeType(), role, branch));
		logger.info("Created employee id={} for branch id={}", employee.getId(), branch.getId());
		return employeeResponse(employee);
	}

	@Override
	@Transactional
	public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
		logger.debug("Updating employee id={}", id);
		Employee employee = employees.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		String emailAddress = request.emailAddress().trim().toLowerCase();
		if (employees.existsByEmailAddressIgnoreCaseAndIdNot(emailAddress, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
		}
		Branch branch = branches.findById(request.branchId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
		EmployeeRole role = request.employeeType() == Employee.EmployeeType.CUSTOMER
				? EmployeeRole.CUSTOMER
				: EmployeeRole.USER;
		employee.updateDetails(request.firstName().trim(), request.lastName().trim(), request.address(),
				request.mobileNo(), emailAddress, request.employeeType(), role, branch);
		Employee updatedEmployee = employees.save(employee);
		logger.info("Updated employee id={} for branch id={}", id, branch.getId());
		return employeeResponse(updatedEmployee);
	}

	private CompanyResponse companyResponse(Company company) {
		return new CompanyResponse(company.getId(), company.getName(), company.getIndustry(), company.getAddress(),
				company.getMobileNo(), company.getEmailAddress(), branches.countByCompanyId(company.getId()));
	}

	private CompanyResponse companyResponse(Company company, long branchCount) {
		return new CompanyResponse(company.getId(), company.getName(), company.getIndustry(), company.getAddress(),
				company.getMobileNo(), company.getEmailAddress(), branchCount);
	}

	private BranchResponse branchResponse(Branch branch) {
		Company company = branch.getCompany();
		return new BranchResponse(branch.getId(), company.getId(), company.getName(), branch.getName(),
				branch.getAddress(), branch.getMobileNo(), branch.getEmailAddress());
	}

	private EmployeeResponse employeeResponse(Employee employee) {
		Branch branch = employee.getBranch();
		return new EmployeeResponse(employee.getId(), branch.getId(), branch.getName(), employee.getFirstName(),
				employee.getLastName(), employee.getAddress(), employee.getMobileNo(), employee.getEmailAddress(),
				employee.getEmployeeType(), employee.getRole(), employee.mustChangePassword());
	}

	private Pageable pageable(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
		}
		return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
	}
}
