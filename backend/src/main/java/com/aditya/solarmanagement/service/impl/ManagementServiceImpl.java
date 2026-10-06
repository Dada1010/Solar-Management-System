package com.aditya.solarmanagement.service.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.aditya.solarmanagement.dto.EffectiveRateRequest;
import com.aditya.solarmanagement.dto.EffectiveRateResponse;
import com.aditya.solarmanagement.dto.MsedclDetailRequest;
import com.aditya.solarmanagement.dto.MsedclDetailResponse;
import com.aditya.solarmanagement.dto.PageResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.models.Branch;
import com.aditya.solarmanagement.models.Company;
import com.aditya.solarmanagement.models.Employee;
import com.aditya.solarmanagement.models.EmployeeRole;
import com.aditya.solarmanagement.models.EffectiveRate;
import com.aditya.solarmanagement.models.EffectiveRateOwnerType;
import com.aditya.solarmanagement.models.MsedclDetail;
import com.aditya.solarmanagement.repo.BranchRepository;
import com.aditya.solarmanagement.repo.CompanyRepository;
import com.aditya.solarmanagement.repo.EmployeeRepository;
import com.aditya.solarmanagement.repo.EffectiveRateRepository;
import com.aditya.solarmanagement.repo.MsedclDetailRepository;
import com.aditya.solarmanagement.repo.specification.BranchSpecifications;
import com.aditya.solarmanagement.repo.specification.EmployeeSpecifications;
import com.aditya.solarmanagement.repo.specification.MsedclDetailSpecifications;
import com.aditya.solarmanagement.service.ManagementService;
import com.aditya.solarmanagement.service.InvoiceService;

@Service
@Transactional(readOnly = true)
public class ManagementServiceImpl implements ManagementService {
	private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class);
	private final CompanyRepository companies;
	private final BranchRepository branches;
	private final EmployeeRepository employees;
	private final MsedclDetailRepository msedclDetails;
	private final InvoiceService invoiceService;
	private final EffectiveRateRepository effectiveRates;
	private final PasswordEncoder passwordEncoder;

	public ManagementServiceImpl(CompanyRepository companies, BranchRepository branches, EmployeeRepository employees,
			MsedclDetailRepository msedclDetails, InvoiceService invoiceService,
			EffectiveRateRepository effectiveRates, PasswordEncoder passwordEncoder) {
		this.companies = companies;
		this.branches = branches;
		this.employees = employees;
		this.msedclDetails = msedclDetails;
		this.invoiceService = invoiceService;
		this.effectiveRates = effectiveRates;
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
	@Transactional
	public void deleteCompany(Long id) {
		Company company = companies.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
		if (branches.existsByCompanyId(id) || effectiveRates.existsByCompany_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Companies with branches or effective rates cannot be deleted");
		}
		companies.delete(company);
		logger.info("Deleted company id={}", id);
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
	@Transactional
	public void deleteBranch(Long id) {
		Branch branch = branches.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
		if (employees.existsByBranch_Id(id) || effectiveRates.existsByBranch_Id(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Branches with employees or effective rates cannot be deleted");
		}
		branches.delete(branch);
		logger.info("Deleted branch id={}", id);
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

	@Override
	@Transactional
	public void deleteEmployee(Long id) {
		Employee employee = employees.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		boolean hasInvoiceHistory = employee.getMsedclDetails().stream()
				.anyMatch(detail -> invoiceService.hasInvoicesForDetail(detail.getId()));
		if (hasInvoiceHistory) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Customers with invoice history cannot be deleted");
		}
		employees.delete(employee);
		logger.info("Deleted employee id={}", id);
	}

	@Override
	@Transactional
	public MessageResponse resetEmployeePassword(Long id) {
		Employee employee = employees.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		String temporaryPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		employee.setPasswordHash(passwordEncoder.encode(temporaryPassword));
		employee.setMustChangePassword(true);
		employees.save(employee);
		logger.info("Reset password for employee id={} type={}", id, employee.getEmployeeType());
		return new MessageResponse("Temporary password: " + temporaryPassword
				+ ". The account must set a new password after signing in.");
	}

	@Override
	public List<MsedclDetailResponse> consumerDetails(String requestingEmail, String name, String mobileNo,
			String consumerNo) {
		Employee requester = employeeByEmail(requestingEmail);
		Long branchId = requester.getRole() == EmployeeRole.USER ? requester.getBranch().getId() : null;
		Long customerId = requester.getRole() == EmployeeRole.CUSTOMER ? requester.getId() : null;
		List<MsedclDetail> details = findConsumerDetails(branchId, customerId, name, mobileNo, consumerNo);
		logger.debug("Listed consumer details requesterId={} role={} count={}", requester.getId(), requester.getRole(),
				details.size());
		return details.stream().map(this::msedclDetailResponse).toList();
	}

	@Override
	public List<MsedclDetailResponse> msedclDetails(Long customerId, String requestingEmail, String name,
			String mobileNo, String consumerNo) {
		Employee requester = employeeByEmail(requestingEmail);
		Employee customer = customer(customerId);
		boolean allowed = switch (requester.getRole()) {
			case ADMIN -> true;
			case USER -> requester.getBranch().getId().equals(customer.getBranch().getId());
			case CUSTOMER -> requester.getId().equals(customer.getId());
		};
		if (!allowed) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this customer's details");
		}
		logger.debug("Listing MSEDCL details customerId={} requesterId={}", customerId, requester.getId());
		return findConsumerDetails(null, customerId, name, mobileNo, consumerNo).stream()
				.map(this::msedclDetailResponse).toList();
	}

	private List<MsedclDetail> findConsumerDetails(Long branchId, Long customerId, String name, String mobileNo,
			String consumerNo) {
		return msedclDetails.findAll(MsedclDetailSpecifications.byBranchId(branchId)
				.and(MsedclDetailSpecifications.byCustomerId(customerId))
				.and(MsedclDetailSpecifications.byName(name))
				.and(MsedclDetailSpecifications.byMobileNo(mobileNo))
				.and(MsedclDetailSpecifications.byConsumerNo(consumerNo)), Sort.by(Sort.Direction.ASC, "id"));
	}

	@Override
	@Transactional
	public MsedclDetailResponse addMsedclDetail(Long customerId, MsedclDetailRequest request) {
		Employee customer = customer(customerId);
		String consumerNo = request.consumerNo().trim();
		if (msedclDetails.existsByConsumerNoIgnoreCase(consumerNo)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This consumer number is already in use");
		}
		MsedclDetail detail = new MsedclDetail(request.billingUnit().trim(), request.name().trim(),
				request.mobileNo().trim(), consumerNo, request.ratePerUnit(), request.chargeType(), request.dueDays());
		customer.addMsedclDetail(detail);
		employees.save(customer);
		logger.info("Added MSEDCL detail id={} for customer id={}", detail.getId(), customerId);
		return msedclDetailResponse(detail);
	}

	@Override
	@Transactional
	public MsedclDetailResponse updateMsedclDetail(Long customerId, Long detailId, MsedclDetailRequest request) {
		Employee customer = customer(customerId);
		MsedclDetail detail = customer.getMsedclDetails().stream()
				.filter(item -> item.getId().equals(detailId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MSEDCL detail not found"));
		String consumerNo = request.consumerNo().trim();
		if (msedclDetails.existsByConsumerNoIgnoreCaseAndIdNot(consumerNo, detailId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This consumer number is already in use");
		}
		detail.updateDetails(request.billingUnit().trim(), request.name().trim(), request.mobileNo().trim(),
				consumerNo, request.ratePerUnit(), request.chargeType(), request.dueDays());
		logger.info("Updated MSEDCL detail id={} for customer id={}", detailId, customerId);
		return msedclDetailResponse(detail);
	}

	@Override
	@Transactional
	public void deleteMsedclDetail(Long customerId, Long detailId) {
		Employee customer = customer(customerId);
		MsedclDetail detail = customer.getMsedclDetails().stream()
				.filter(item -> item.getId().equals(detailId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MSEDCL detail not found"));
		if (invoiceService.hasInvoicesForDetail(detailId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "MSEDCL details with invoice history cannot be deleted");
		}
		customer.removeMsedclDetail(detail);
		logger.info("Removed MSEDCL detail id={} from customer id={}", detailId, customerId);
	}

	private Employee customer(Long customerId) {
		Employee employee = employees.findById(customerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
		if (employee.getEmployeeType() != Employee.EmployeeType.CUSTOMER) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MSEDCL details can only be added to customers");
		}
		return employee;
	}

	private Employee employeeByEmail(String emailAddress) {
		return employees.findByEmailAddressIgnoreCase(emailAddress)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
	}

	private MsedclDetailResponse msedclDetailResponse(MsedclDetail detail) {
		Branch branch = detail.getCustomer().getBranch();
		return new MsedclDetailResponse(detail.getId(), branch.getCompany().getId(), branch.getId(), branch.getName(),
				detail.getBillingUnit(), detail.getName(), detail.getMobileNo(), detail.getConsumerNo(),
				detail.getRatePerUnit(), detail.getLastInvoiceNo(), detail.getDueDays(), detail.getChargeType());
	}

	@Override
	public List<EffectiveRateResponse> effectiveRates(EffectiveRateOwnerType ownerType, Long ownerId) {
		logger.debug("Listing effective rates ownerType={} ownerId={}", ownerType, ownerId);
		return ratesFor(ownerType, ownerId).stream().map(this::effectiveRateResponse).toList();
	}

	@Override
	@Transactional
	public EffectiveRateResponse addEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId,
			EffectiveRateRequest request) {
		RateOwner owner = rateOwner(ownerType, ownerId);
		if (rateExists(ownerType, ownerId, request.startDate(), null)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A rate already exists for this start date");
		}
		EffectiveRate rate = new EffectiveRate(request.startDate(), request.ratePerUnit(), owner.company(), owner.branch(),
				owner.msedclDetail());
		EffectiveRate saved = effectiveRates.save(rate);
		logger.info("Added effective rate id={} ownerType={} ownerId={}", saved.getId(), ownerType, ownerId);
		return effectiveRateResponse(saved);
	}

	@Override
	@Transactional
	public EffectiveRateResponse updateEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId,
			EffectiveRateRequest request) {
		EffectiveRate rate = ratesFor(ownerType, ownerId).stream()
				.filter(item -> item.getId().equals(rateId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Effective rate not found"));
		if (rateExists(ownerType, ownerId, request.startDate(), rateId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A rate already exists for this start date");
		}
		rate.updateDetails(request.startDate(), request.ratePerUnit());
		logger.info("Updated effective rate id={} ownerType={} ownerId={}", rateId, ownerType, ownerId);
		return effectiveRateResponse(rate);
	}

	@Override
	@Transactional
	public void deleteEffectiveRate(EffectiveRateOwnerType ownerType, Long ownerId, Long rateId) {
		EffectiveRate rate = ratesFor(ownerType, ownerId).stream()
				.filter(item -> item.getId().equals(rateId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Effective rate not found"));
		effectiveRates.delete(rate);
		logger.info("Deleted effective rate id={} ownerType={} ownerId={}", rateId, ownerType, ownerId);
	}

	private List<EffectiveRate> ratesFor(EffectiveRateOwnerType ownerType, Long ownerId) {
		rateOwner(ownerType, ownerId);
		return switch (ownerType) {
			case COMPANY -> effectiveRates.findAllByCompany_IdOrderByStartDateDescIdDesc(ownerId);
			case BRANCH -> effectiveRates.findAllByBranch_IdOrderByStartDateDescIdDesc(ownerId);
			case MSEDCL_DETAIL -> effectiveRates.findAllByMsedclDetail_IdOrderByStartDateDescIdDesc(ownerId);
		};
	}

	private RateOwner rateOwner(EffectiveRateOwnerType ownerType, Long ownerId) {
		return switch (ownerType) {
			case COMPANY -> new RateOwner(companies.findById(ownerId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found")), null, null);
			case BRANCH -> new RateOwner(null, branches.findById(ownerId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found")), null);
			case MSEDCL_DETAIL -> new RateOwner(null, null, msedclDetails.findById(ownerId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MSEDCL detail not found")));
		};
	}

	private boolean rateExists(EffectiveRateOwnerType ownerType, Long ownerId, java.time.LocalDate startDate,
			Long excludedRateId) {
		return switch (ownerType) {
			case COMPANY -> excludedRateId == null
					? effectiveRates.existsByCompany_IdAndStartDate(ownerId, startDate)
					: effectiveRates.existsByCompany_IdAndStartDateAndIdNot(ownerId, startDate, excludedRateId);
			case BRANCH -> excludedRateId == null
					? effectiveRates.existsByBranch_IdAndStartDate(ownerId, startDate)
					: effectiveRates.existsByBranch_IdAndStartDateAndIdNot(ownerId, startDate, excludedRateId);
			case MSEDCL_DETAIL -> excludedRateId == null
					? effectiveRates.existsByMsedclDetail_IdAndStartDate(ownerId, startDate)
					: effectiveRates.existsByMsedclDetail_IdAndStartDateAndIdNot(ownerId, startDate, excludedRateId);
		};
	}

	private EffectiveRateResponse effectiveRateResponse(EffectiveRate rate) {
		return new EffectiveRateResponse(rate.getId(), rate.getStartDate(), rate.getRatePerUnit());
	}

	private record RateOwner(Company company, Branch branch, MsedclDetail msedclDetail) {}

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
		return new EmployeeResponse(employee.getId(), branch.getCompany().getId(), branch.getId(), branch.getName(),
				employee.getFirstName(), employee.getLastName(), employee.getAddress(), employee.getMobileNo(), employee.getEmailAddress(),
				employee.getEmployeeType(), employee.getRole(), employee.mustChangePassword());
	}

	private Pageable pageable(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
		}
		return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
	}
}
