package com.aditya.solarmanagement.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.aditya.solarmanagement.dto.MsedclInvoiceRequest;
import com.aditya.solarmanagement.dto.MsedclInvoiceResponse;
import com.aditya.solarmanagement.dto.InvoicePaymentRequest;
import com.aditya.solarmanagement.dto.InvoicePaymentResponse;
import com.aditya.solarmanagement.models.InvoicePaymentStatus;
import com.aditya.solarmanagement.models.InvoiceStatus;
import java.time.LocalDate;

public interface InvoiceService {
	List<MsedclInvoiceResponse> invoices(String requestingEmail, String invoiceNo, String consumerName,
			String consumerNo, InvoicePaymentStatus paymentStatus, InvoiceStatus invoiceStatus,
			LocalDate invoiceDateFrom, LocalDate invoiceDateTo);
	MsedclInvoiceResponse previewInvoice(String requestingEmail, MsedclInvoiceRequest request);
	MsedclInvoiceResponse createInvoice(String requestingEmail, MsedclInvoiceRequest request);
	void cancelInvoice(String requestingEmail, Long invoiceId);
	MsedclInvoiceResponse uploadMsebBill(String requestingEmail, Long invoiceId, MultipartFile file);
	InvoiceAttachmentDownload downloadMsebBill(String requestingEmail, Long invoiceId);
	List<InvoicePaymentResponse> paymentHistory(String requestingEmail, Long invoiceId);
	List<InvoicePaymentResponse> allPayments(String requestingEmail);
	List<InvoicePaymentResponse> addPayment(String requestingEmail, Long invoiceId, InvoicePaymentRequest request);
	void cancelPayment(String requestingEmail, Long invoiceId, Long paymentId);
	boolean hasInvoicesForDetail(Long detailId);

	record InvoiceAttachmentDownload(Resource resource, String fileName, String contentType) {}
}