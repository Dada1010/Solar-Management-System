package com.aditya.solarmanagement.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.solarmanagement.models.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {
	List<Branch> findAllByCompanyId(Long companyId);
	long countByCompanyId(Long companyId);
	boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(Long companyId, String name, Long id);

	@Query("select b.company.id, count(b.id) from Branch b where b.company.id in :companyIds group by b.company.id")
	List<Object[]> countBranchesByCompanyIds(@Param("companyIds") Collection<Long> companyIds);

	@Override
	@EntityGraph(attributePaths = "company")
	Page<Branch> findAll(Specification<Branch> specification, Pageable pageable);
}
