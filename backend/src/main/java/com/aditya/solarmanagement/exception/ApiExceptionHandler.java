package com.aditya.solarmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.aditya.solarmanagement.dto.ApiResponse;

@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
		logger.warn("Request failed with status {}: {}", status.value(), message);
		return ResponseEntity.status(status).body(ApiResponse.failure(status, message));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.findFirst()
				.orElse("Request validation failed");
		logger.warn("Request validation failed: {}", message);
		return ResponseEntity.badRequest().body(ApiResponse.failure(HttpStatus.BAD_REQUEST, message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadableBody() {
		logger.warn("Request body could not be parsed");
		return ResponseEntity.badRequest()
				.body(ApiResponse.failure(HttpStatus.BAD_REQUEST, "Request body is invalid"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedError(Exception exception) {
		logger.error("Unexpected API error", exception);
		return ResponseEntity.internalServerError()
				.body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
	}
}