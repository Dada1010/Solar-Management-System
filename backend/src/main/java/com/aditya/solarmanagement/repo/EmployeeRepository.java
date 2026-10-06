package com.aditya.solarmanagement.repo;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.aditya.solarmanagement.models.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
	Optional<Employee> findByEmailAddressIgnoreCase(String emailAddress);
	boolean existsByEmailAddressIgnoreCase(String emailAddress);
	boolean existsByEmailAddressIgnoreCaseAndIdNot(String emailAddress, Long id);
	boolean existsByBranch_Id(Long branchId);
	long countByEmployeeType(Employee.EmployeeType employeeType);
	long countByEmployeeTypeAndBranch_Id(Employee.EmployeeType employeeType, Long branchId);
	List<Employee> findAllByEmployeeType(Employee.EmployeeType employeeType);

	@Override
	@EntityGraph(attributePaths = "branch")
	Page<Employee> findAll(Specification<Employee> specification, Pageable pageable);
}
