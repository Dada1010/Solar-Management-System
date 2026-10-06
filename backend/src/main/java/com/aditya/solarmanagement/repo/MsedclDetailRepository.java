package com.aditya.solarmanagement.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.MsedclDetail;

public interface MsedclDetailRepository extends JpaRepository<MsedclDetail, Long> {
	boolean existsByConsumerNoIgnoreCase(String consumerNo);
	boolean existsByConsumerNoIgnoreCaseAndIdNot(String consumerNo, Long detailId);
}