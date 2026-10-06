package com.aditya.solarmanagement.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface InvoiceAttachmentStorageService {
	StoredInvoiceAttachment store(MultipartFile file);
	Resource load(String storageName);
	void delete(String storageName);

	record StoredInvoiceAttachment(String storageName, String originalFileName, String contentType) {}
}