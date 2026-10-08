package com.aditya.solarmanagement.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.solarmanagement.models.MsedclInvoiceOtherCharge;

public interface MsedclInvoiceOtherChargeRepository extends JpaRepository<MsedclInvoiceOtherCharge, Long> {
	boolean existsByReason_Id(Long reasonId);
}
