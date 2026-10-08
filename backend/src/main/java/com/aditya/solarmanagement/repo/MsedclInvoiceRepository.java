package com.aditya.solarmanagement.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.solarmanagement.models.MsedclInvoice;
import com.aditya.solarmanagement.models.InvoiceStatus;

public interface MsedclInvoiceRepository extends JpaRepository<MsedclInvoice, Long>,
		JpaSpecificationExecutor<MsedclInvoice> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select invoice from MsedclInvoice invoice where invoice.id = :invoiceId")
	Optional<MsedclInvoice> findByIdForUpdate(@Param("invoiceId") Long invoiceId);
	boolean existsByMsedclDetail_IdAndBillingDateAndStatus(Long detailId, LocalDate billingDate, InvoiceStatus status);
	boolean existsByMsedclDetail_Id(Long detailId);
	boolean existsByReferral_Id(Long referralId);
	List<MsedclInvoice> findAllByOrderByInvoiceDateDescIdDesc();
	List<MsedclInvoice> findAllByMsedclDetail_Customer_Branch_IdOrderByInvoiceDateDescIdDesc(Long branchId);
	List<MsedclInvoice> findAllByMsedclDetail_Customer_IdOrderByInvoiceDateDescIdDesc(Long customerId);
}