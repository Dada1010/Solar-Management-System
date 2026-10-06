package com.aditya.solarmanagement.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileNumberValidator implements ConstraintValidator<ValidMobileNumber, String> {
	private static final Pattern NORMALIZED_NUMBER = Pattern.compile("\\+?[0-9]{7,15}");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		String normalized = value.replaceAll("[\\s()-]", "");
		return NORMALIZED_NUMBER.matcher(normalized).matches();
	}
}