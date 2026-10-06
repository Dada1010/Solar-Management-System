package com.aditya.solarmanagement.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(boolean status, int code, String message, T data) {
	public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
		return new ApiResponse<>(httpStatus.is2xxSuccessful(), httpStatus.value(), message, data);
	}

	public static <T> ApiResponse<T> failure(HttpStatus httpStatus, String message) {
		return new ApiResponse<>(false, httpStatus.value(), message, null);
	}
}