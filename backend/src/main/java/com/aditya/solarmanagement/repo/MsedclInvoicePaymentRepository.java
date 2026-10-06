package com.aditya.solarmanagement.repo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.solarmanagement.models.MsedclInvoicePayment;

public interface MsedclInvoicePaymentRepository extends JpaRepository<MsedclInvoicePayment, Long> {
	boolean existsByInvoice_Id(Long invoiceId);
	boolean existsByReversalOfPayment_Id(Long paymentId);
	List<MsedclInvoicePayment> findAllByInvoice_IdOrderByPaymentDateDescIdDesc(Long invoiceId);
	List<MsedclInvoicePayment> findAllByOrderByPaymentDateDescIdDesc();
	List<MsedclInvoicePayment> findAllByInvoice_Branch_IdOrderByPaymentDateDescIdDesc(Long branchId);
	List<MsedclInvoicePayment> findAllByInvoice_MsedclDetail_Customer_IdOrderByPaymentDateDescIdDesc(Long customerId);

	@Query("select p.invoice.id, sum(p.amount) from MsedclInvoicePayment p where p.invoice.id in :invoiceIds group by p.invoice.id")
	List<Object[]> totalsByInvoiceIds(@Param("invoiceIds") Collection<Long> invoiceIds);

	@Query("select coalesce(sum(p.amount), 0) from MsedclInvoicePayment p where p.invoice.id = :invoiceId")
	BigDecimal totalByInvoiceId(@Param("invoiceId") Long invoiceId);
}