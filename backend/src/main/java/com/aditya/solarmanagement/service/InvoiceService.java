package com.aditya.solarmanagement.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.aditya.solarmanagement.dto.MsedclInvoiceRequest;
import com.aditya.solarmanagement.dto.MsedclInvoiceResponse;
import com.aditya.solarmanagement.dto.InvoicePaymentRequest;
import com.aditya.solarmanagement.dto.InvoicePaymentResponse;

public interface InvoiceService {
	List<MsedclInvoiceResponse> invoices(String requestingEmail);
	MsedclInvoiceResponse previewInvoice(String requestingEmail, MsedclInvoiceRequest request);
	MsedclInvoiceResponse createInvoice(String requestingEmail, MsedclInvoiceRequest request);
	MsedclInvoiceResponse uploadMsebBill(String requestingEmail, Long invoiceId, MultipartFile file);
	InvoiceAttachmentDownload downloadMsebBill(String requestingEmail, Long invoiceId);
	List<InvoicePaymentResponse> paymentHistory(String requestingEmail, Long invoiceId);
	List<InvoicePaymentResponse> addPayment(String requestingEmail, Long invoiceId, InvoicePaymentRequest request);
	boolean hasInvoicesForDetail(Long detailId);

	record InvoiceAttachmentDownload(Resource resource, String fileName, String contentType) {}
}