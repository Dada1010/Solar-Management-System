package com.aditya.solarmanagement.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.service.InvoiceAttachmentStorageService;

@Service
public class InvoiceAttachmentStorageServiceImpl implements InvoiceAttachmentStorageService {
	private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
	private final Path storageDirectory;

	public InvoiceAttachmentStorageServiceImpl(
			@Value("${app.upload.invoice-directory:uploads/invoices}") String storageDirectory) {
		this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
	}

	@Override
	public StoredInvoiceAttachment store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an MSEB bill file to upload");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "MSEB bill files must be 10 MB or smaller");
		}
		String contentType = file.getContentType();
		String extension = switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
			case "application/pdf" -> ".pdf";
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			default -> throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
					"Only PDF, JPG, and PNG MSEB bills are supported");
		};
		String storageName = UUID.randomUUID() + extension;
		Path destination = storageDirectory.resolve(storageName).normalize();
		try {
			Files.createDirectories(storageDirectory);
			Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store MSEB bill", exception);
		}
		String originalName = StringUtils.getFilename(file.getOriginalFilename());
		if (!StringUtils.hasText(originalName)) originalName = "mseb-bill" + extension;
		originalName = originalName.replaceAll("[^A-Za-z0-9._ -]", "_");
		if (originalName.length() > 255) originalName = originalName.substring(originalName.length() - 255);
		return new StoredInvoiceAttachment(storageName, originalName, contentType);
	}

	@Override
	public Resource load(String storageName) {
		Path file = resolve(storageName);
		if (!Files.isRegularFile(file)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MSEB bill file not found");
		}
		return new FileSystemResource(file);
	}

	@Override
	public void delete(String storageName) {
		try {
			Files.deleteIfExists(resolve(storageName));
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete previous MSEB bill", exception);
		}
	}

	private Path resolve(String storageName) {
		if (storageName == null || !storageName.matches("[a-fA-F0-9-]+\\.(pdf|jpg|png)")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid MSEB bill reference");
		}
		Path file = storageDirectory.resolve(storageName).normalize();
		if (!file.getParent().equals(storageDirectory)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid MSEB bill reference");
		}
		return file;
	}
}